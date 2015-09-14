package com.github.kaiwinter.napsterreleases.ui.controller;

import com.github.kaiwinter.napsterreleases.ui.cellvaluefactory.WatchedArtistCellValueFactory;
import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist;

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
	private TableColumn<WatchedArtist, String> artistTc;

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
		SortedList<WatchedArtist> sortedList = new SortedList<>(FXCollections.observableArrayList());
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

		artistTc.setCellValueFactory(new WatchedArtistCellValueFactory.NameValueFactory());
		releasedTc.setCellValueFactory(new WatchedArtistCellValueFactory.WatchedArtistValueFactory());
		albumTc.setCellValueFactory(new WatchedArtistCellValueFactory.WatchedArtistValueFactory());

		releasedTc.setCellFactory(new WatchedArtistCellValueFactory.LastReleaseCellFactory());
		albumTc.setCellFactory(new WatchedArtistCellValueFactory.AlbumNameCellFactory());

		viewModel.loadArtistWatchlist();
	}

	@FXML
	public void clearArtistWatchlist() {
		viewModel.clearArtistWatchlist();
	}
}
