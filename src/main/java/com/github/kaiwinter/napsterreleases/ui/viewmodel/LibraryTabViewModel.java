package com.github.kaiwinter.napsterreleases.ui.viewmodel;

import java.util.Collection;
import java.util.Comparator;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.jfx.tablecolumn.filter.FilterSupport;
import com.github.kaiwinter.rhapsody.model.AlbumData;
import com.github.kaiwinter.rhapsody.model.AlbumData.Artist;

import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

@Singleton
public final class LibraryTabViewModel implements ViewModel {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryTabViewModel.class.getSimpleName());

	private final BooleanProperty loading = new SimpleBooleanProperty();

	private final ListProperty<Artist> artists = new SimpleListProperty<>();
	private final ListProperty<AlbumData> releases = new SimpleListProperty<>();

	private final ObjectProperty<Artist> selectedArtist = new SimpleObjectProperty<>();
	private final ObjectProperty<AlbumData> selectedAlbum = new SimpleObjectProperty<>();

	@Inject
	private SharedViewModel sharedViewModel;

	public BooleanProperty loadingProperty() {
		return this.loading;
	}

	public ListProperty<AlbumData> releasesProperty() {
		return this.releases;
	}

	public ListProperty<Artist> artistsProperty() {
		return this.artists;
	}

	public ObjectProperty<Artist> selectedArtistProperty() {
		return this.selectedArtist;
	}

	public ObjectProperty<AlbumData> selectedAlbumProperty() {
		return this.selectedAlbum;
	}

	public void loadAllArtistsInLibrary() {
		loadingProperty().set(true);
		Callback<Collection<Artist>> callback = new Callback<Collection<Artist>>() {

			@Override
			public void success(Collection<Artist> artists, Response response) {
				LOGGER.info("Loaded {} artists", artists.size());
				ObservableList<Artist> observableArrayList = FXCollections.observableArrayList(artists);
				Comparator<Artist> comparator = (o1, o2) -> o1.name.compareTo(o2.name);
				artistsProperty().set(observableArrayList.sorted(comparator));
				loadingProperty().set(false);
			}

			@Override
			public void failure(RetrofitError error) {
				LOGGER.error(error.getMessage(), error);
				loadingProperty().set(false);
				sharedViewModel.handleError(error, () -> loadAllArtistsInLibrary());
			}
		};
		sharedViewModel.getRhapsodySdkWrapper().loadAllArtistsInLibrary(null, callback);
	}

	public void loadAlbumsOfSelectedArtist(Artist artist) {
		loadingProperty().set(true);
		Callback<Collection<AlbumData>> callback = new Callback<Collection<AlbumData>>() {

			@Override
			public void success(Collection<AlbumData> albums, Response response) {
				LOGGER.info("Loaded {} albums", albums.size());
				// Check if selection changed in the meantime
				Artist currentGenre = selectedArtistProperty().get();
				if (currentGenre != null && currentGenre == artist) {
					ObservableList<AlbumData> items = (ObservableList<AlbumData>) FilterSupport.getUnwrappedList(releasesProperty().get());
					Platform.runLater(() -> items.setAll(albums));
				} else {
					LOGGER.info("Artist selection changed, not showing loaded data");
				}
				loadingProperty().set(false);
			}

			@Override
			public void failure(RetrofitError error) {
				LOGGER.error(error.getMessage(), error);
				loadingProperty().set(false);
				sharedViewModel.handleError(error, () -> loadAlbumsOfSelectedArtist(artist));
			}
		};
		sharedViewModel.getRhapsodySdkWrapper().loadAllAlbumsByArtistInLibrary(artist.id, null, callback);
	}
}
