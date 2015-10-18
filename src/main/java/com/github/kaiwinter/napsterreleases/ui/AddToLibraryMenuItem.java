package com.github.kaiwinter.napsterreleases.ui;

import com.github.kaiwinter.napsterreleases.ui.viewmodel.LibraryTabViewModel;
import com.github.kaiwinter.rhapsody.model.AlbumData;

import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;

public final class AddToLibraryMenuItem extends MenuItem {

	public AddToLibraryMenuItem(TableView<AlbumData> tableView, LibraryTabViewModel LibraryTabViewModel) {
		setText("Add Album To Library");

		setOnAction(event -> {
			AlbumData albumData = tableView.getSelectionModel().getSelectedItem();
			LibraryTabViewModel.addAlbumToLibrary(albumData);
		});
	}
}
