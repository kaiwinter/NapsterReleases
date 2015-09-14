package com.github.kaiwinter.napsterreleases.ui.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.kaiwinter.napsterreleases.persistence.WatchedArtistsStore;
import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist;
import com.github.kaiwinter.napsterreleases.util.TimeUtil;
import com.github.kaiwinter.rhapsody.model.AlbumData;
import com.github.kaiwinter.rhapsody.model.AlbumData.Artist;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;

@Singleton
public final class ArtistWatchlistTabViewModel implements ViewModel {
	private final BooleanProperty loading = new SimpleBooleanProperty();
	private final ListProperty<WatchedArtist> watchedArtists = new SimpleListProperty<>();

	@Inject
	private SharedViewModel sharedViewModel;

	@Inject
	private WatchedArtistsStore watchedArtistsStore;

	public BooleanProperty loadingProperty() {
		return this.loading;
	}

	public ListProperty<WatchedArtist> watchedArtistsProperty() {
		return this.watchedArtists;
	}

	public void loadArtistWatchlist() {
		Set<WatchedArtist> watchedArtists = watchedArtistsStore.loadWatchedArtists();

		ObservableList<WatchedArtist> sortedList = watchedArtistsProperty().get();
		ObservableList<WatchedArtist> sourceList = (ObservableList<WatchedArtist>) ((SortedList<WatchedArtist>) sortedList).getSource();
		sourceList.setAll(watchedArtists);
	}

	private void loadLastRelease(WatchedArtist watchedArtist) {
		Collection<AlbumData> artistNewReleases = sharedViewModel.getRhapsodySdkWrapper().getArtistNewReleases(watchedArtist.getArtist().id,
				1);
		if (artistNewReleases.size() > 0) {
			AlbumData albumData = artistNewReleases.iterator().next();
			watchedArtist.getLastRelease().setDate(TimeUtil.timestampToString(albumData.released));
			watchedArtist.getLastRelease().setAlbumName(albumData.name);
		}
	}

	public void removeArtistFromWatchlist(WatchedArtist selectedArtist) {
		Set<WatchedArtist> watchedArtists = watchedArtistsStore.loadWatchedArtists();
		watchedArtists = watchedArtists.stream().filter(artist -> !selectedArtist.getArtist().id.equals(artist.getArtist().id))
				.collect(Collectors.toSet());
		watchedArtistsStore.saveWatchedArtists(watchedArtists);

		loadArtistWatchlist();
	}

	public void addArtistToWatchlist(Artist artistToWatch) {
		Set<WatchedArtist> watchedArtists = watchedArtistsStore.loadWatchedArtists();

		boolean alreadyAdded = watchedArtists.stream().anyMatch(artist -> artistToWatch.id.equals(artist.getArtist().id));
		if (!alreadyAdded) {
			WatchedArtist watchedArtist = new WatchedArtist(artistToWatch);
			watchedArtists.add(watchedArtist);
			loadLastRelease(watchedArtist);

			watchedArtistsStore.saveWatchedArtists(watchedArtists);
			loadArtistWatchlist();
		}
	}

	public void clearArtistWatchlist() {
		watchedArtistsStore.saveWatchedArtists(Collections.emptySet());

		loadArtistWatchlist();
	}
}
