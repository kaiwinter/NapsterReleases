package com.github.kaiwinter.napsterreleases.ui.view;

import javax.inject.Inject;

import com.github.kaiwinter.jfx.tablecolumn.filter.FilterSupport;
import com.github.kaiwinter.napsterreleases.ui.AddToWatchlistMenuItem;
import com.github.kaiwinter.napsterreleases.ui.AlbumDataCellValueFactories;
import com.github.kaiwinter.napsterreleases.ui.DoubleClickListenerCellFactory;
import com.github.kaiwinter.napsterreleases.ui.viewmodel.ArtistWatchlistTabViewModel;
import com.github.kaiwinter.napsterreleases.ui.viewmodel.LibraryTabViewModel;
import com.github.kaiwinter.napsterreleases.ui.viewmodel.MainViewModel;
import com.github.kaiwinter.rhapsody.model.AlbumData;
import com.github.kaiwinter.rhapsody.model.AlbumData.Artist;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;
import javafx.util.Callback;

/**
 * Controller for the New Releases tab.
 */
public final class LibraryTabView implements FxmlView<LibraryTabViewModel> {

   @FXML
   private ListView<Artist> artistLv;

   @FXML
   private TableView<AlbumData> releasesTv;

   @FXML
   private TableColumn<AlbumData, String> artistTc;
   @FXML
   private TableColumn<AlbumData, String> albumTc;
   @FXML
   private TableColumn<AlbumData, String> releasedTc;

   @FXML
   private ProgressIndicator loadingIndicator;
   @FXML
   private Region loadingIndicatorBackground;

   @Inject
   private MainViewModel mainViewModel;

   @Inject
   private ArtistWatchlistTabViewModel artistWatchlistTabViewModel;

   @InjectViewModel
   private LibraryTabViewModel viewModel;

   @FXML
   public void initialize() {
      bindProperties();
      setFactories();

      SortedList<AlbumData> sorted = FXCollections.<AlbumData> observableArrayList().filtered(null).sorted();
      releasesTv.setItems(sorted);
      sorted.comparatorProperty().bind(releasesTv.comparatorProperty());
      viewModel.releasesProperty().bind(releasesTv.itemsProperty());

      viewModel.artistsProperty().bindBidirectional(artistLv.itemsProperty());

      FilterSupport.addFilter(artistTc);
      FilterSupport.addFilter(albumTc);
      FilterSupport.addFilter(releasedTc);

      viewModel.tabSelectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
         // Automatically load if tab is selected and no data was loaded previously
         if (newValue && artistLv.getItems().isEmpty()) {
            loadAllAlbumsInLibrary();
         }
      });

      artistLv.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
         FilterSupport.getItems(releasesTv).clear();

         if (newValue != null) {
            viewModel.loadAlbumsOfSelectedArtist(newValue);
         }
      });

      releasesTv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
   }

   private void setFactories() {
      releasesTv.setRowFactory(tv -> {
         ContextMenu contextMenu = new ContextMenu();
         contextMenu.getItems().add(new AddToWatchlistMenuItem(releasesTv, artistWatchlistTabViewModel));
         MenuItem removeMenuItem = new MenuItem("Remove from Library");
         removeMenuItem.setOnAction(event -> {
            ObservableList<AlbumData> albumData = releasesTv.getSelectionModel().getSelectedItems();
            viewModel.removeAlbumsFromLibrary(albumData);
         });
         contextMenu.getItems().add(removeMenuItem);
         TableRow<AlbumData> row = new TableRow<>();
         row.contextMenuProperty().bind(
            Bindings.when(Bindings.isNotNull(row.itemProperty())).then(contextMenu).otherwise((ContextMenu) null));

         return row;
      });

      artistLv.setCellFactory(new Callback<ListView<Artist>, ListCell<Artist>>() {

         @Override
         public ListCell<Artist> call(ListView<Artist> param) {
            ListCell<Artist> listCell = new ListCell<Artist>() {
               @Override
               protected void updateItem(Artist item, boolean empty) {
                  super.updateItem(item, empty);
                  if (empty) {
                     setText(null);
                  } else {
                     setText(item.name);
                  }
               }
            };

            ContextMenu contextMenu = new ContextMenu();
            MenuItem removeMenuItem = new MenuItem("Remove from Library");
            removeMenuItem.setOnAction(event -> {
               Artist artist = artistLv.getSelectionModel().getSelectedItem();
               viewModel.removeArtistFromLibrary(artist);
            });
            contextMenu.getItems().add(removeMenuItem);
            listCell.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(listCell.itemProperty()))
               .then(contextMenu).otherwise((ContextMenu) null));

            return listCell;
         }
      });

      artistTc.setCellValueFactory(new AlbumDataCellValueFactories.ArtistNameValueFactory());
      albumTc.setCellValueFactory(new AlbumDataCellValueFactories.AlbumNameValueFactory());
      releasedTc.setCellValueFactory(new AlbumDataCellValueFactories.ReleaseDateValueFactory());

      artistTc.setCellFactory(
         new DoubleClickListenerCellFactory<AlbumData, String>(() -> mainViewModel.switchToArtistTab()));
      albumTc
         .setCellFactory(new DoubleClickListenerCellFactory<AlbumData, String>(() -> mainViewModel.switchToAlbumTab()));
   }

   private void bindProperties() {
      viewModel.releasesProperty().bindBidirectional(releasesTv.itemsProperty());

      viewModel.selectedArtistProperty().bind(artistLv.getSelectionModel().selectedItemProperty());
      viewModel.selectedAlbumProperty().bind(releasesTv.getSelectionModel().selectedItemProperty());

      viewModel.loadingProperty().bindBidirectional(loadingIndicator.visibleProperty());
      viewModel.loadingProperty().bindBidirectional(loadingIndicatorBackground.visibleProperty());
   }

   @FXML
   private void loadAllAlbumsInLibrary() {
      viewModel.loadAllArtistsInLibrary();
   }

   @FXML
   private void exportLibrary() {
      viewModel.exportLibrary();
   }

   @FXML
   private void importLibrary() {
      viewModel.importLibrary();
   }
}