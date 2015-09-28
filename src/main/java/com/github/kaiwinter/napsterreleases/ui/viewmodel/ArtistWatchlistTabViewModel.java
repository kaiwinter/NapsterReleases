package com.github.kaiwinter.napsterreleases.ui.viewmodel;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.kaiwinter.napsterreleases.persistence.WatchedArtistsStore;
import com.github.kaiwinter.napsterreleases.ui.NotificationPaneIcon;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;

@Singleton
public final class ArtistWatchlistTabViewModel implements ViewModel {
	private final BooleanProperty loading = new SimpleBooleanProperty();
	private final ListProperty<WatchedArtist> watchedArtists = new SimpleListProperty<>(
			FXCollections.<WatchedArtist> observableArrayList().sorted());

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
		@SuppressWarnings("unchecked")
		ObservableList<WatchedArtist> sourceList = (ObservableList<WatchedArtist>) ((SortedList<WatchedArtist>) sortedList).getSource();
		sourceList.setAll(watchedArtists);
	}

	private LastRelease loadLastRelease(WatchedArtist watchedArtist) {
		Collection<AlbumData> artistNewReleases = sharedViewModel.getRhapsodySdkWrapper().getArtistNewReleases(watchedArtist.getArtist().id,
				1);
		if (artistNewReleases.size() > 0) {
			AlbumData albumData = artistNewReleases.iterator().next();
			LastRelease lastRelease = new LastRelease();
			lastRelease.setAlbumName(albumData.name);
			lastRelease.setDate(TimeUtil.timestampToString(albumData.released));
			return lastRelease;
		}
		return null;
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
			LastRelease lastRelease = loadLastRelease(watchedArtist);
			if (lastRelease != null) {
				watchedArtist.getLastRelease().setAlbumName(lastRelease.getAlbumName());
				watchedArtist.getLastRelease().setDate(lastRelease.getDate());
			}

			watchedArtistsStore.saveWatchedArtists(watchedArtists);
			loadArtistWatchlist();
		}
	}

	public void clearArtistWatchlist() {
		watchedArtistsStore.saveWatchedArtists(Collections.emptySet());

		loadArtistWatchlist();
	}

	/**
	 * Checks if the watched artists have new released albums. A notification is shown if there are new releases.
	 */
	public void checkForNewReleases() {
		loadArtistWatchlist();

		watchedArtists.forEach(watchedArtist -> watchedArtist.getLastRelease().setTextColor(Color.LIGHTGRAY));

		Task<Void> task = new UpdateWatchedArtistsTask();
		new Thread(task).start();
	}

	private class UpdateWatchedArtistsTask extends Task<Void> {
		@Override
		protected Void call() throws Exception {
			int updates = 0;
			for (WatchedArtist watchedArtist : watchedArtists) {
				LastRelease currentLastRelease = watchedArtist.getLastRelease();
				LastRelease lastRelease = loadLastRelease(watchedArtist);
				if (currentLastRelease != null) {
					if (currentLastRelease.equals(lastRelease)) {
						currentLastRelease.setTextColor(Color.BLACK);
					} else {
						updates++;
						Platform.runLater(() -> {
							if (lastRelease != null) {
								currentLastRelease.setAlbumName(lastRelease.getAlbumName());
								currentLastRelease.setDate(lastRelease.getDate());
								currentLastRelease.setTextColor(Color.RED);
							}
						});
					}
				}
			}
			if (updates > 0) {
				watchedArtistsStore.saveWatchedArtists(new HashSet<>(watchedArtists));
				String message = MessageFormat
						.format("There {0,choice, 1#is|1<are} {0,number,integer} new album {0,choice, 1#release|1<releases}", updates);

				sharedViewModel.showAutoHidingNotification(NotificationPaneIcon.INFO, message);
			}

			return null;
		}
	}
}
