package com.github.kaiwinter.napsterreleases.ui.controller;

import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist;
import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist.LastRelease;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValueBase;
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
public final class ArtistWatchlistTabView {

	@FXML
	private TableView<WatchedArtist> artistsTv;

	@FXML
	private TableColumn<WatchedArtist, String> artistTc;

	@FXML
	private TableColumn<WatchedArtist, String> releasedTc;

	@FXML
	private TableColumn<WatchedArtist, String> albumTc;

	@FXML
	private ProgressIndicator loadingIndicator;

	@FXML
	private Region loadingIndicatorBackground;

	private ArtistWatchlistTabViewModel viewModel;

	private MainController mainController;

	@FXML
	private void initialize() {
		this.viewModel = new ArtistWatchlistTabViewModel();

		SortedList<WatchedArtist> sortedList = new SortedList<>(FXCollections.observableArrayList());
		artistsTv.setItems(sortedList);
		sortedList.comparatorProperty().bind(artistsTv.comparatorProperty());
		this.viewModel.watchedArtists().bindBidirectional(artistsTv.itemsProperty());

		artistsTv.getSortOrder().add(artistTc);

		artistsTv.setRowFactory(tv -> {
			TableRow<WatchedArtist> row = new TableRow<>();

			ContextMenu artistColumnContextMenu = new ContextMenu();
			MenuItem addToWatchlistMenuItem = new MenuItem("Remove from Watchlist");
			addToWatchlistMenuItem.setOnAction((e) -> {
				WatchedArtist selectedArtist = artistsTv.getSelectionModel().getSelectedItem();
				mainController.removeArtistFromWatchlist(selectedArtist);
			});
			artistColumnContextMenu.getItems().add(addToWatchlistMenuItem);
			row.contextMenuProperty().bind(
					Bindings.when(Bindings.isNotNull(row.itemProperty())).then(artistColumnContextMenu).otherwise((ContextMenu) null));

			return row;
		});

		artistTc.setCellValueFactory(value -> new ObservableValueBase<String>() {
			@Override
			public String getValue() {
				return value.getValue().artist.name;
			}
		});

		releasedTc.setCellValueFactory(value -> new ObservableValueBase<String>() {
			@Override
			public String getValue() {
				LastRelease lastRelease = value.getValue().lastRelease;
				if (lastRelease == null) {
					return null;
				}
				return lastRelease.date;
			}
		});

		albumTc.setCellValueFactory(value -> new ObservableValueBase<String>() {
			@Override
			public String getValue() {
				LastRelease lastRelease = value.getValue().lastRelease;
				if (lastRelease == null) {
					return null;
				}
				return lastRelease.albumName;
			}
		});
	}

	@FXML
	public void clearArtistWatchlist() {
		mainController.clearArtistWatchlist();
	}

	public ArtistWatchlistTabViewModel getViewModel() {
		return viewModel;
	}

	public void setMainController(MainController mainController) {
		this.mainController = mainController;
	}
}