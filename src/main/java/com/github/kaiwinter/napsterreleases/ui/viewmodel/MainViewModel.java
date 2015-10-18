package com.github.kaiwinter.napsterreleases.ui.viewmodel;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist;
import com.github.kaiwinter.napsterreleases.ui.view.MainView;
import com.github.kaiwinter.rhapsody.model.AlbumData;
import com.github.kaiwinter.rhapsody.model.AlbumData.Artist;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.Stage;

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
	private ArtistWatchlistTabViewModel artistWatchlistTabViewModel;

	@Inject
	private MainView mainView;

	private Stage primaryStage;

	public void bindSelectedAlbumProperty() {
		// Use listener instead of binding to connect to sources with one target
		ChangeListener<AlbumData> changeListener = (observable, oldValue, newValue) -> {
			artistTabViewModel.selectedAlbumProperty().set(newValue);
			albumTabViewModel.selectedAlbumProperty().set(newValue);
		};

		newReleasesTabViewModel.selectedAlbumProperty().addListener(changeListener);
		libraryTabViewModel.selectedAlbumProperty().addListener(changeListener);
		ChangeListener<WatchedArtist> listener = new ChangeListener<WatchedArtist>() {

			@Override
			public void changed(ObservableValue<? extends WatchedArtist> observable, WatchedArtist oldValue, WatchedArtist newValue) {
				if (newValue == null) {
					return;
				}
				AlbumData albumData = new AlbumData();
				albumData.id = newValue.getLastRelease().getId();
				albumData.artist = new Artist();
				albumData.artist.id = newValue.getArtist().id;
				artistTabViewModel.selectedAlbumProperty().set(albumData);
				albumTabViewModel.selectedAlbumProperty().set(albumData);
			}
		};
		artistWatchlistTabViewModel.selectedWatchedArtistProperty().addListener(listener);
	}

	public void switchToArtistTab() {
		mainView.switchToArtistTab();
	}

	public void switchToAlbumTab() {
		mainView.switchToAlbumTab();
	}

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	public Stage getPrimarySage() {
		return primaryStage;
	}
}
