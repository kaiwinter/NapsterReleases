package com.github.kaiwinter.napsterreleases.ui.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.napsterreleases.UserSettings;
import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist;
import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist.LastRelease;
import com.github.kaiwinter.napsterreleases.util.TimeUtil;
import com.github.kaiwinter.rhapsody.model.AlbumData;
import com.github.kaiwinter.rhapsody.model.AlbumData.Artist;

import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;

@Singleton
public final class ArtistWatchlistTabViewModel implements ViewModel {
	private static final Logger LOGGER = LoggerFactory.getLogger(ArtistWatchlistTabViewModel.class.getSimpleName());

	private final BooleanProperty loading = new SimpleBooleanProperty();
	private final ListProperty<WatchedArtist> watchedArtists = new SimpleListProperty<>();

	@Inject
	private SharedViewModel sharedViewModel;

	@Inject
	private UserSettings userSettings;

	/**
	 * Caches the last release of an artist.
	 */
	private final Map<String, LastRelease> artistId2ReleaseDateCache = new HashMap<>();

	public BooleanProperty loadingProperty() {
		return this.loading;
	}

	public ListProperty<WatchedArtist> watchedArtistsProperty() {
		return this.watchedArtists;
	}

	public void loadArtistWatchlist() {
		Set<Artist> artists = userSettings.loadWatchedArtists();

		Set<WatchedArtist> watchedArtists = artists.stream().map(artist -> new WatchedArtist(artist)).collect(Collectors.toSet());
		ObservableList<WatchedArtist> sortedList = watchedArtistsProperty().get();
		ObservableList<WatchedArtist> sourceList = (ObservableList<WatchedArtist>) ((SortedList<WatchedArtist>) sortedList).getSource();
		sourceList.setAll(watchedArtists);

		loadReleaseDates();
	}

	private void loadReleaseDates() {

		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				for (WatchedArtist watchedArtist : watchedArtists) {
					LastRelease lastReleaseFromCache = artistId2ReleaseDateCache.get(watchedArtist.getArtist().id);

					if (lastReleaseFromCache == null) {
						Collection<AlbumData> artistNewReleases = sharedViewModel.getRhapsodySdkWrapper()
								.getArtistNewReleases(watchedArtist.getArtist().id, 1);
						if (artistNewReleases.size() > 0) {
							AlbumData albumData = artistNewReleases.iterator().next();
							LastRelease lastRelease = new LastRelease();
							lastRelease.setDate(TimeUtil.timestampToString(albumData.released));
							lastRelease.setAlbumName(albumData.name);
							artistId2ReleaseDateCache.put(watchedArtist.getArtist().id, lastRelease);
							Platform.runLater(() -> {
								watchedArtist.getLastRelease().setDate(TimeUtil.timestampToString(albumData.released));
								watchedArtist.getLastRelease().setAlbumName(albumData.name);
							});
						}
					} else {
						Platform.runLater(() -> {
							watchedArtist.getLastRelease().setDate(lastReleaseFromCache.getDate());
							watchedArtist.getLastRelease().setAlbumName(lastReleaseFromCache.getAlbumName());
						});
					}
				}
				return null;
			}

			@Override
			protected void done() {
				try {
					if (!isCancelled()) {
						// call get to catch a possible exception
						get();
					}
				} catch (ExecutionException e) {
					LOGGER.error(e.getMessage(), e);
				} catch (InterruptedException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		};
		new Thread(task).start();
	}

	public void removeArtistFromWatchlist(WatchedArtist selectedArtist) {
		Set<Artist> watchedArtists = userSettings.loadWatchedArtists();
		watchedArtists = watchedArtists.stream().filter(artist -> !selectedArtist.getArtist().id.equals(artist.id))
				.collect(Collectors.toSet());
		userSettings.saveWatchedArtists(watchedArtists);

		loadArtistWatchlist();
	}

	public void addArtistToWatchlist(Artist artistToWatch) {
		Set<Artist> watchedArtists = userSettings.loadWatchedArtists();

		boolean alreadyAdded = watchedArtists.stream().anyMatch(artist -> artistToWatch.id.equals(artist.id));
		if (!alreadyAdded) {
			watchedArtists.add(artistToWatch);
			userSettings.saveWatchedArtists(watchedArtists);

			loadArtistWatchlist();
		}
	}

	public void clearArtistWatchlist() {
		userSettings.saveWatchedArtists(Collections.emptySet());

		loadArtistWatchlist();
	}
}
