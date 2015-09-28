package com.github.kaiwinter.napsterreleases.ui.viewmodel;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.kaiwinter.napsterreleases.ui.view.MainView;
import com.github.kaiwinter.rhapsody.model.AlbumData;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.value.ChangeListener;

@Singleton
public final class MainViewModel implements ViewModel {

	@Inject
	private NewReleasesTabViewModel newReleasesTabViewModel;

	@Inject
	private ArtistTabViewModel artistTabViewModel;

	@Inject
	private AlbumTabViewModel albumTabViewModel;

	@Inject
	private LibraryTabViewModel libraryTabViewModel;

	@Inject
	private MainView mainView;

	public void bindSelectedAlbumProperty() {
		// Use listener instead of binding to connect to sources with one target
		ChangeListener<AlbumData> changeListener = (observable, oldValue, newValue) -> {
			artistTabViewModel.selectedAlbumProperty().set(newValue);
			albumTabViewModel.selectedAlbumProperty().set(newValue);
		};

		newReleasesTabViewModel.selectedAlbumProperty().addListener(changeListener);
		libraryTabViewModel.selectedAlbumProperty().addListener(changeListener);
	}

	public void switchToArtistTab() {
		mainView.switchToArtistTab();
	}

	public void switchToAlbumTab() {
		mainView.switchToAlbumTab();
	}
}
