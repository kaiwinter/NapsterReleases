package com.github.kaiwinter.napsterreleases.ui.view;

import com.github.kaiwinter.napsterreleases.ui.WatchedArtistCellValueFactory;
import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist;
import com.github.kaiwinter.napsterreleases.ui.viewmodel.ArtistWatchlistTabViewModel;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;

/**
 * Controller for the artist watchlist tab.
 */
public final class ArtistWatchlistTabView implements FxmlView<ArtistWatchlistTabViewModel> {

	@FXML
	private TableView<WatchedArtist> artistsTv;

	@FXML
	private TableColumn<WatchedArtist, WatchedArtist> artistTc;

	@FXML
	private TableColumn<WatchedArtist, WatchedArtist> releasedTc;

	@FXML
	private TableColumn<WatchedArtist, WatchedArtist> albumTc;

	@FXML
	private ProgressIndicator loadingIndicator;

	@FXML
	private Region loadingIndicatorBackground;

	@InjectViewModel
	private ArtistWatchlistTabViewModel viewModel;

	@FXML
	public void initialize() {
		SortedList<WatchedArtist> sortedList = FXCollections.<WatchedArtist> observableArrayList().sorted();
		artistsTv.setItems(sortedList);
		sortedList.comparatorProperty().bind(artistsTv.comparatorProperty());
		viewModel.watchedArtistsProperty().bindBidirectional(artistsTv.itemsProperty());

		artistsTv.setRowFactory(tv -> {
			TableRow<WatchedArtist> row = new TableRow<>();

			ContextMenu artistColumnContextMenu = new ContextMenu();
			MenuItem addToWatchlistMenuItem = new MenuItem("Remove from Watchlist");
			addToWatchlistMenuItem.setOnAction((e) -> {
				WatchedArtist selectedArtist = artistsTv.getSelectionModel().getSelectedItem();
				viewModel.removeArtistFromWatchlist(selectedArtist);
			});
			artistColumnContextMenu.getItems().add(addToWatchlistMenuItem);
			row.contextMenuProperty().bind(
					Bindings.when(Bindings.isNotNull(row.itemProperty())).then(artistColumnContextMenu).otherwise((ContextMenu) null));

			return row;
		});

		artistTc.setCellValueFactory(new WatchedArtistCellValueFactory.WatchedArtistValueFactory());
		releasedTc.setCellValueFactory(new WatchedArtistCellValueFactory.WatchedArtistValueFactory());
		albumTc.setCellValueFactory(new WatchedArtistCellValueFactory.WatchedArtistValueFactory());

		artistTc.setCellFactory(c -> new WatchedArtistCellValueFactory.ArtistNameCellFactory());
		releasedTc.setCellFactory(c -> new WatchedArtistCellValueFactory.LastReleaseCellFactory());
		albumTc.setCellFactory(c -> new WatchedArtistCellValueFactory.AlbumNameCellFactory());

		artistTc.setComparator((o1, o2) -> o1.getArtist().name.compareTo(o2.getArtist().name));
		releasedTc.setComparator((o1, o2) -> {
			int result = o1.getLastRelease().getDate().compareTo(o2.getLastRelease().getDate());
			if (result == 0) {
				// If dates are equal, keep previous sorting by artist name
				result = artistTc.getComparator().compare(o1, o2);
			}
			return result;
		});
		albumTc.setComparator((o1, o2) -> o1.getLastRelease().getAlbumName().compareTo(o2.getLastRelease().getAlbumName()));

		viewModel.loadArtistWatchlist();
	}

	@FXML
	public void clearArtistWatchlist() {
		viewModel.clearArtistWatchlist();
	}

	@FXML
	public void checkUpdates() {
		viewModel.checkForNewReleases();
	}
}
