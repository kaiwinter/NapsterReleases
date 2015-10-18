package com.github.kaiwinter.napsterreleases.ui;

import com.github.kaiwinter.napsterreleases.ui.viewmodel.ArtistWatchlistTabViewModel;
import com.github.kaiwinter.rhapsody.model.AlbumData;

import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;

public final class AddToWatchlistMenuItem extends MenuItem {

	public AddToWatchlistMenuItem(TableView<AlbumData> tableView, ArtistWatchlistTabViewModel artistWatchlistTabViewModel) {
		setText("Add Artist to Watchlist");

		setOnAction((e) -> {
			AlbumData selectedItem = tableView.getSelectionModel().getSelectedItem();
			artistWatchlistTabViewModel.addArtistToWatchlist(selectedItem.artist);
		});
	}
}
