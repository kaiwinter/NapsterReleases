package com.github.kaiwinter.napsterreleases.ui.view;

import javax.inject.Inject;

import com.github.kaiwinter.jfx.tablecolumn.filter.FilterSupport;
import com.github.kaiwinter.napsterreleases.ui.AddToLibraryMenuItem;
import com.github.kaiwinter.napsterreleases.ui.AddToWatchlistMenuItem;
import com.github.kaiwinter.napsterreleases.ui.AlbumDataCellValueFactories;
import com.github.kaiwinter.napsterreleases.ui.DoubleClickListenerCellFactory;
import com.github.kaiwinter.napsterreleases.ui.viewmodel.ArtistWatchlistTabViewModel;
import com.github.kaiwinter.napsterreleases.ui.viewmodel.LibraryTabViewModel;
import com.github.kaiwinter.napsterreleases.ui.viewmodel.MainViewModel;
import com.github.kaiwinter.napsterreleases.ui.viewmodel.NewReleasesTabViewModel;
import com.github.kaiwinter.rhapsody.model.AlbumData;
import com.github.kaiwinter.rhapsody.model.GenreData;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Region;

/**
 * Controller for the New Releases tab.
 */
public final class NewReleasesTabView implements FxmlView<NewReleasesTabViewModel> {

	@FXML
	private TreeView<GenreData> genreList;

	@FXML
	private TextArea textArea;

	@FXML
	private TableView<AlbumData> releasesTv;

	@FXML
	private TableColumn<AlbumData, String> artistTc;
	@FXML
	private TableColumn<AlbumData, String> albumTc;
	@FXML
	private TableColumn<AlbumData, String> releasedTc;
	@FXML
	private TableColumn<AlbumData, String> typeTc;
	@FXML
	private TableColumn<AlbumData, String> discsTc;

	@FXML
	private ProgressIndicator loadingIndicator;
	@FXML
	private Region loadingIndicatorBackground;

	@Inject
	private MainViewModel mainViewModel;

	@Inject
	private ArtistWatchlistTabViewModel artistWatchlistTabViewModel;

	@Inject
	private LibraryTabViewModel libraryTabViewModel;

	@InjectViewModel
	private NewReleasesTabViewModel viewModel;

	@FXML
	public void initialize() {
		bindProperties();
		setFactories();

		genreList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			FilterSupport.getItems(releasesTv).clear();

			if (newValue == null) {
				textArea.clear();
			} else {
				textArea.setText(newValue.getValue().description);
				viewModel.showNewReleases(newValue.getValue());
			}
		});

		SortedList<AlbumData> sorted = FXCollections.<AlbumData> observableArrayList().filtered(null).sorted();
		releasesTv.setItems(sorted);
		sorted.comparatorProperty().bind(releasesTv.comparatorProperty());
		viewModel.releasesProperty().bind(releasesTv.itemsProperty());

		FilterSupport.addFilter(artistTc);
		FilterSupport.addFilter(albumTc);
		FilterSupport.addFilter(releasedTc);
		FilterSupport.addFilter(typeTc);

		viewModel.loadColumnVisibility();
		viewModel.addColumnVisibilityListeners();
	}

	private void setFactories() {
		genreList.setCellFactory(param -> {
			TreeCell<GenreData> listCell = new TreeCell<GenreData>() {
				@Override
				protected void updateItem(GenreData item, boolean empty) {
					super.updateItem(item, empty);
					if (empty) {
						setText(null);
					} else {
						setText(item.name);
					}
				};
			};
			return listCell;
		});

		releasesTv.setRowFactory(tv -> {
			TableRow<AlbumData> row = new TableRow<>();
			ContextMenu contextMenu = new ContextMenu();
			contextMenu.getItems().add(new AddToWatchlistMenuItem(releasesTv, artistWatchlistTabViewModel));
			contextMenu.getItems().add(new AddToLibraryMenuItem(releasesTv, libraryTabViewModel));
			row.contextMenuProperty()
			.bind(Bindings.when(Bindings.isNotNull(row.itemProperty())).then(contextMenu).otherwise((ContextMenu) null));

			return row;
		});

		artistTc.setCellValueFactory(new AlbumDataCellValueFactories.ArtistNameValueFactory());
		albumTc.setCellValueFactory(new AlbumDataCellValueFactories.AlbumNameValueFactory());
		releasedTc.setCellValueFactory(new AlbumDataCellValueFactories.ReleaseDateValueFactory());
		typeTc.setCellValueFactory(new AlbumDataCellValueFactories.TypeValueFactory());
		discsTc.setCellValueFactory(new AlbumDataCellValueFactories.DiscCountValueFactory());

		artistTc.setCellFactory(new DoubleClickListenerCellFactory<AlbumData, String>(() -> mainViewModel.switchToArtistTab()));
		albumTc.setCellFactory(new DoubleClickListenerCellFactory<AlbumData, String>(() -> mainViewModel.switchToAlbumTab()));
	}

	private void bindProperties() {
		viewModel.genresProperty().bindBidirectional(genreList.rootProperty());
		viewModel.releasesProperty().bindBidirectional(releasesTv.itemsProperty());
		viewModel.genreDescriptionProperty().bindBidirectional(textArea.textProperty());

		viewModel.selectedGenreProperty().bind(genreList.getSelectionModel().selectedItemProperty());
		viewModel.selectedAlbumProperty().bind(releasesTv.getSelectionModel().selectedItemProperty());

		viewModel.loadingProperty().bindBidirectional(loadingIndicator.visibleProperty());
		viewModel.loadingProperty().bindBidirectional(loadingIndicatorBackground.visibleProperty());

		viewModel.artistColumVisibleProperty().bindBidirectional(artistTc.visibleProperty());
		viewModel.albumColumVisibleProperty().bindBidirectional(albumTc.visibleProperty());
		viewModel.releasedColumVisibleProperty().bindBidirectional(releasedTc.visibleProperty());
		viewModel.typeColumVisibleProperty().bindBidirectional(typeTc.visibleProperty());
		viewModel.discsColumVisibleProperty().bindBidirectional(discsTc.visibleProperty());

		viewModel.tabSelectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
			// Automatically load if tab is selected and no data was loaded previously
			if (newValue && genreList.getRoot() == null) {
				loadGenres();
			}
		});
	}

	@FXML
	private void loadGenres() {
		viewModel.loadGenres();
	}

	@FXML
	private void logout() {
		viewModel.logout();
	}

}
