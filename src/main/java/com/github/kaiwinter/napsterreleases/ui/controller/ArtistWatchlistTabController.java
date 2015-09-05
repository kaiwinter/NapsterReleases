package com.github.kaiwinter.napsterreleases.ui.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.napsterreleases.UserSettings;
import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist;
import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist.LastRelease;
import com.github.kaiwinter.napsterreleases.util.TimeUtil;
import com.github.kaiwinter.rhapsody.api.RhapsodySdkWrapper;
import com.github.kaiwinter.rhapsody.model.AlbumData;
import com.github.kaiwinter.rhapsody.model.AlbumData.Artist;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;

/**
 * Controller for the artist watchlist tab.
 */
public final class ArtistWatchlistTabController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArtistWatchlistTabController.class.getSimpleName());

	@FXML
	private TableView<WatchedArtist> artistsTv;

	@FXML
	private TableColumn<WatchedArtist, String> artistTc;

	@FXML
	private TableColumn<WatchedArtist, String> releasedTc;

	@FXML
	private TableColumn<WatchedArtist, String> albumTc;

	@FXML
	private ProgressIndicator loadingIndicator;
	@FXML
	private Region loadingIndicatorBackground;

	private UserSettings userSettings;

	private RhapsodySdkWrapper rhapsodySdkWrapper;

	/**
	 * Caches the last release of an artist.
	 */
	private Map<String, LastRelease> artistId2ReleaseDateCache = new HashMap<>();

	@FXML
	private void initialize() {
		userSettings = new UserSettings();

		artistsTv.getSortOrder().add(artistTc);

		artistsTv.setRowFactory(tv -> {
			TableRow<WatchedArtist> row = new TableRow<>();

			ContextMenu artistColumnContextMenu = new ContextMenu();
			MenuItem addToWatchlistMenuItem = new MenuItem("Remove from Watchlist");
			addToWatchlistMenuItem.setOnAction((e) -> {
				WatchedArtist selectedArtist = artistsTv.getSelectionModel().getSelectedItem();
				removeArtistFromWatchlist(selectedArtist);
			});
			artistColumnContextMenu.getItems().add(addToWatchlistMenuItem);
			row.contextMenuProperty().bind(
					Bindings.when(Bindings.isNotNull(row.itemProperty())).then(artistColumnContextMenu).otherwise((ContextMenu) null));

			return row;
		});

		artistTc.setCellValueFactory(value -> new ObservableValueBase<String>() {
			@Override
			public String getValue() {
				return value.getValue().artist.name;
			}
		});

		releasedTc.setCellValueFactory(value -> new ObservableValueBase<String>() {
			@Override
			public String getValue() {
				LastRelease lastRelease = value.getValue().lastRelease;
				if (lastRelease == null) {
					return null;
				}
				return lastRelease.date;
			}
		});

		albumTc.setCellValueFactory(value -> new ObservableValueBase<String>() {
			@Override
			public String getValue() {
				LastRelease lastRelease = value.getValue().lastRelease;
				if (lastRelease == null) {
					return null;
				}
				return lastRelease.albumName;
			}
		});
	}

	private void removeArtistFromWatchlist(WatchedArtist selectedArtist) {
		Set<Artist> watchedArtists = userSettings.loadWatchedArtists();
		watchedArtists = watchedArtists.stream().filter(artist -> !selectedArtist.artist.id.equals(artist.id)).collect(Collectors.toSet());
		userSettings.saveWatchedArtists(watchedArtists);

		loadWatchedArtists();
	}

	public void addArtistToWatchlist(Artist artistToWatch) {
		Set<Artist> watchedArtists = userSettings.loadWatchedArtists();

		boolean alreadyAdded = watchedArtists.stream().anyMatch(artist -> artistToWatch.id.equals(artist.id));
		if (!alreadyAdded) {
			watchedArtists.add(artistToWatch);
			userSettings.saveWatchedArtists(watchedArtists);

			loadWatchedArtists();
		}
	}

	@FXML
	public void clearArtistWatchlist() {
		userSettings.saveWatchedArtists(Collections.emptySet());

		loadWatchedArtists();
	}

	public void loadWatchedArtists() {
		Set<Artist> artists = userSettings.loadWatchedArtists();
		List<TableColumn<WatchedArtist, ?>> savesSortOrder = new ArrayList<>(artistsTv.getSortOrder());

		Set<WatchedArtist> watchedArtists = artists.stream().map(artist -> new WatchedArtist(artist)).collect(Collectors.toSet());
		ObservableList<WatchedArtist> observableWatchedArtists = FXCollections.observableArrayList(watchedArtists);
		artistsTv.setItems(observableWatchedArtists);

		artistsTv.getSortOrder().addAll(savesSortOrder);
		artistsTv.sort();

		loadReleaseDates();
	}

	private void loadReleaseDates() {

		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				ObservableList<WatchedArtist> observableWatchedArtists = artistsTv.getItems();
				for (WatchedArtist watchedArtist : observableWatchedArtists) {
					watchedArtist.lastRelease = artistId2ReleaseDateCache.get(watchedArtist.artist.id);

					if (watchedArtist.lastRelease == null) {
						Collection<AlbumData> artistNewReleases = rhapsodySdkWrapper.getArtistNewReleases(watchedArtist.artist.id, 1);
						if (artistNewReleases.size() > 0) {
							AlbumData albumData = artistNewReleases.iterator().next();
							watchedArtist.lastRelease = new LastRelease();
							watchedArtist.lastRelease.date = TimeUtil.timestampToString(albumData.released);
							watchedArtist.lastRelease.albumName = albumData.name;
							artistId2ReleaseDateCache.put(watchedArtist.artist.id, watchedArtist.lastRelease);
						}
					}
					// FIXME KW: Hack needed for JDK < 8.60
					// https://bugs.openjdk.java.net/browse/JDK-8098235
					Platform.runLater(() -> {
						artistsTv.getColumns().get(2).setVisible(false);
						artistsTv.getColumns().get(2).setVisible(true);
					});
				}
				return null;
			}

			@Override
			protected void done() {
				try {
					if (!isCancelled())
						get();
				} catch (ExecutionException e) {
					LOGGER.error(e.getMessage(), e);
				} catch (InterruptedException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		};
		new Thread(task).start();
	}

	public void setRhapsodySdkWrapper(RhapsodySdkWrapper rhapsodySdkWrapper) {
		this.rhapsodySdkWrapper = rhapsodySdkWrapper;
	}
}
