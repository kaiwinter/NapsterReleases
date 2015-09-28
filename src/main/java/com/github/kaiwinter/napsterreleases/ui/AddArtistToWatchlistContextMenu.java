package com.github.kaiwinter.napsterreleases.ui;

import com.github.kaiwinter.napsterreleases.ui.viewmodel.ArtistWatchlistTabViewModel;
import com.github.kaiwinter.rhapsody.model.AlbumData;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;

public final class AddArtistToWatchlistContextMenu extends ContextMenu {
	public AddArtistToWatchlistContextMenu(TableView<AlbumData> tableView, ArtistWatchlistTabViewModel artistWatchlistTabViewModel) {
		setOnAction((e) -> {
			AlbumData selectedItem = tableView.getSelectionModel().getSelectedItem();
			artistWatchlistTabViewModel.addArtistToWatchlist(selectedItem.artist);
		});

		MenuItem addToWatchlistMenuItem = new MenuItem("Add Artist to Watchlist");
		getItems().add(addToWatchlistMenuItem);
	}
}
