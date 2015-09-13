package com.github.kaiwinter.napsterreleases.ui.controller;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.saxsys.mvvmfx.ViewModel;

@Singleton
public final class MainViewModel implements ViewModel {

	@Inject
	private NewReleasesTabViewModel newReleasesTabViewModel;

	@Inject
	private ArtistTabViewModel artistTabViewModel;

	@Inject
	private AlbumTabViewModel albumTabViewModel;

	@Inject
	private MainView mainView;

	public void bindSelectedAlbumProperty() {
		artistTabViewModel.selectedAlbumProperty().bind(newReleasesTabViewModel.selectedAlbumProperty());
		albumTabViewModel.selectedAlbumProperty().bind(newReleasesTabViewModel.selectedAlbumProperty());
	}

	public void switchToArtistTab() {
		mainView.switchToArtistTab();
	}

	public void switchToAlbumTab() {
		mainView.switchToAlbumTab();
	}
}
