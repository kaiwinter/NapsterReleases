package com.github.kaiwinter.napsterreleases.ui.controller;

import java.io.IOException;

import com.github.kaiwinter.napsterreleases.UserSettings;
import com.github.kaiwinter.rhapsody.model.AlbumData.Artist;

public final class MainViewModel {

	private final MainView mainController;

	private final UserSettings userSettings;

	private final NewReleasesTabViewModel newReleasesTabViewModel;
	private final ArtistTabViewModel artistTabViewModel;
	private final AlbumTabViewModel albumTabViewModel;
	private final ArtistWatchlistTabViewModel artistWatchlistTabViewModel;

	public MainViewModel(MainView mainController, NewReleasesTabView newReleasesTabController, ArtistTabView artistTabController,
			AlbumTabView albumTabController, ArtistWatchlistTabView artistWatchlistTabController) throws IOException {
		this.mainController = mainController;
		this.userSettings = new UserSettings();

		// API Wrapper Access
		SharedViewModel sharedViewModel = new SharedViewModel(mainController);

		// Instantiate ViewModels
		newReleasesTabViewModel = new NewReleasesTabViewModel(sharedViewModel, userSettings);
		albumTabViewModel = new AlbumTabViewModel(sharedViewModel);
		artistTabViewModel = new ArtistTabViewModel(sharedViewModel);
		artistWatchlistTabViewModel = new ArtistWatchlistTabViewModel(sharedViewModel, userSettings);

		// Set ViewModels in Views
		newReleasesTabController.setViewModel(newReleasesTabViewModel);
		artistTabController.setViewModel(artistTabViewModel);
		albumTabController.setViewModel(albumTabViewModel);
		artistWatchlistTabController.setViewModel(artistWatchlistTabViewModel);

		newReleasesTabController.setMainViewModel(this);

		artistTabViewModel.selectedAlbumProperty().bind(newReleasesTabViewModel.selectedAlbumProperty());
		albumTabViewModel.selectedAlbumProperty().bind(newReleasesTabViewModel.selectedAlbumProperty());

		newReleasesTabViewModel.loadGenres();

		newReleasesTabViewModel.loadColumnVisibility();
		newReleasesTabViewModel.addColumnVisibilityListeners();

		artistWatchlistTabViewModel.loadArtistWatchlist();
	}

	public void switchToArtistTab() {
		mainController.switchToArtistTab();
	}

	public void switchToAlbumTab() {
		mainController.switchToAlbumTab();
	}

	public void showArtist() {
		artistTabViewModel.showArtist();
	}

	public void showAlbum() {
		albumTabViewModel.showAlbum();
	}

	public void addArtistToWatchlist(Artist artist) {
		artistWatchlistTabViewModel.addArtistToWatchlist(artist);
	}
}
