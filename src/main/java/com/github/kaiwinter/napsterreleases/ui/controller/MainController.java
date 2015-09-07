package com.github.kaiwinter.napsterreleases.ui.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.controlsfx.control.NotificationPane;
import org.controlsfx.dialog.ExceptionDialog;
import org.controlsfx.dialog.LoginDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.jfx.tablecolumn.filter.FilterSupport;
import com.github.kaiwinter.napsterreleases.RhapsodyApiKeyConfig;
import com.github.kaiwinter.napsterreleases.UserSettings;
import com.github.kaiwinter.napsterreleases.ui.callback.ActionRetryCallback;
import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist;
import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist.LastRelease;
import com.github.kaiwinter.napsterreleases.util.TimeUtil;
import com.github.kaiwinter.rhapsody.api.ArtistImageSize;
import com.github.kaiwinter.rhapsody.api.AuthenticationCallback;
import com.github.kaiwinter.rhapsody.api.RhapsodySdkWrapper;
import com.github.kaiwinter.rhapsody.model.AccountData;
import com.github.kaiwinter.rhapsody.model.AlbumData;
import com.github.kaiwinter.rhapsody.model.AlbumData.Artist;
import com.github.kaiwinter.rhapsody.model.ArtistData;
import com.github.kaiwinter.rhapsody.model.BioData;
import com.github.kaiwinter.rhapsody.model.GenreData;
import com.github.kaiwinter.rhapsody.persistence.impl.PreferencesAuthorizationStore;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.util.Pair;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Main Controller of the application window. It contains three more controllers, one for each tab.
 */
public final class MainController {
	private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class.getSimpleName());

	private static final String RHAPSODY_CURATED = "rhapsody_curated";
	private static final String RHAPSODY_PERSONALIZED = "rhapsody_personalized";

	@FXML
	private BorderPane borderPane;

	@FXML
	private TabPane tabPane;

	@FXML
	private Tab artistTabHandle;

	@FXML
	private Tab albumTabHandle;

	@FXML
	private Tab artistWatchlistTabHandle;

	@FXML
	private NewReleasesTabView newReleasesTabController;

	@FXML
	private ArtistTabView artistTabController;

	@FXML
	private AlbumTabView albumTabController;

	@FXML
	private ArtistWatchlistTabView artistWatchlistTabController;

	@FXML
	private NotificationPane notificationPane;

	private RhapsodySdkWrapper rhapsodySdkWrapper;

	private AccountData userAccountData;

	private UserSettings userSettings;

	/**
	 * Caches the last release of an artist.
	 */
	private Map<String, LastRelease> artistId2ReleaseDateCache = new HashMap<>();

	public MainController() throws IOException {
		// Do this in constructor to get a better error output
		RhapsodyApiKeyConfig rhapsodyApiKeyConfig = new RhapsodyApiKeyConfig();
		rhapsodySdkWrapper = new RhapsodySdkWrapper(rhapsodyApiKeyConfig.apiKey, rhapsodyApiKeyConfig.apiSecret,
				new PreferencesAuthorizationStore());
		// rhapsodySdkWrapper.setVerboseLoggingEnabled(true);
		this.userSettings = new UserSettings();
	}

	@FXML
	private void initialize() {
		newReleasesTabController.setMainController(this);
		artistWatchlistTabController.setMainController(this);

		addTabListeners();

		loadGenres();

		loadColumnVisibility();
		addColumnVisibilityListeners();

		loadArtistWatchlist();

		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> Platform.runLater(() -> {
			LOGGER.error(throwable.getMessage(), throwable);
			ExceptionDialog exceptionDialog = new ExceptionDialog(throwable);
			exceptionDialog.show();
		}));
	}

	private void addTabListeners() {
		artistTabHandle.setOnSelectionChanged(event -> {
			if (artistTabHandle.isSelected()) {
				AlbumData albumData = newReleasesTabController.getViewModel().selectedAlbumProperty().get();
				if (albumData != null) {
					showArtist(albumData.artist.id);
				}
			}
		});

		albumTabHandle.setOnSelectionChanged(event -> {
			if (albumTabHandle.isSelected()) {
				AlbumData albumData = newReleasesTabController.getViewModel().selectedAlbumProperty().get();
				if (albumData != null) {
					showAlbum(albumData.id);
				}
			}
		});
	}

	/**
	 * Tries to re-authenticate by using the refresh_token to get a new access_token. If this fails (or no refresh_token is available) the
	 * user gets asked for his credentials.
	 *
	 * @param actionCallback
	 *            action to execute if re-authentication succeeds
	 */
	public void tryReAuthorization(ActionRetryCallback actionCallback) {
		rhapsodySdkWrapper.refreshToken(new AuthenticationCallback() {
			@Override
			public void success() {
				actionCallback.retryAction();
			}

			@Override
			public void failure(int status, String reason) {
				Platform.runLater(() -> {
					showAutoHidingNotification("notification-pane-warning.png", "Authentication failed (" + status + ") - " + reason);

					authorize(actionCallback);
				});
			}
		});
	}

	private void authorize(ActionRetryCallback actionCallback) {
		LoginDialog loginDialog = new LoginDialog(null, null);
		loginDialog.initOwner(borderPane.getScene().getWindow());
		loginDialog.setTitle("Login");
		loginDialog.setHeaderText("Please enter your Rhapsody or Napster login data");
		Optional<Pair<String, String>> optional = loginDialog.showAndWait();
		if (!optional.isPresent()) {
			return;
		}

		Pair<String, String> pair = optional.get();
		rhapsodySdkWrapper.authorize(pair.getKey(), pair.getValue(), new AuthenticationCallback() {

			@Override
			public void success() {
				Platform.runLater(() -> {
					showAutoHidingNotification("notification-pane-info.png", "Authentication successful");

					actionCallback.retryAction();
				});
			}

			@Override
			public void failure(int status, String reason) {
				Platform.runLater(() -> {
					ButtonType retry = new ButtonType("Retry", ButtonData.OK_DONE);
					ButtonType[] buttons = { retry, ButtonType.CANCEL };
					Alert dlg = new Alert(AlertType.ERROR, "Login failed. Wrong username/password?", buttons);
					dlg.initModality(Modality.APPLICATION_MODAL);
					dlg.initOwner(borderPane.getScene().getWindow());

					dlg.setHeaderText(status + " " + reason);
					Optional<ButtonType> showAndWait = dlg.showAndWait();
					if (showAndWait.isPresent()) {
						if (showAndWait.get().getButtonData() == ButtonData.OK_DONE) {
							authorize(actionCallback);
						}
					} // else: user canceled
				});
			}
		});
	}

	public void logout() {
		rhapsodySdkWrapper.clearAuthorization();
		newReleasesTabController.getViewModel().clearData();
		clearDetailTabs();
	}

	public void loadGenres() {
		newReleasesTabController.getViewModel().clearData();
		newReleasesTabController.getViewModel().loadingProperty().set(true);
		rhapsodySdkWrapper.loadGenres(new Callback<Collection<GenreData>>() {

			@Override
			public void success(Collection<GenreData> genres, Response response) {
				LOGGER.info("Loaded {} genres", genres.size());
				setGenres(genres);
				newReleasesTabController.getViewModel().loadingProperty().set(false);
			}

			@Override
			public void failure(RetrofitError error) {
				newReleasesTabController.getViewModel().loadingProperty().set(false);
				LOGGER.error("Error loading genres ({})", error.getMessage());
				handleError(error, () -> loadGenres());
			}
		});
	}

	public void showNewReleases(GenreData genreData) {
		newReleasesTabController.getViewModel().loadingProperty().set(true);
		Callback<Collection<AlbumData>> callback = new Callback<Collection<AlbumData>>() {

			@Override
			public void success(Collection<AlbumData> albums, Response response) {
				LOGGER.info("Loaded {} albums", albums.size());
				// Check if genre selection changed in the meantime
				GenreData currentGenre = newReleasesTabController.getViewModel().selectedGenreProperty().get().getValue();
				if (currentGenre != null && currentGenre == genreData) {
					Platform.runLater(() -> {
						clearDetailTabs();
						setNewReleases(albums);
						newReleasesTabController.getViewModel().loadingProperty().set(false);
					});
				} else {
					LOGGER.info("Genre selection changed, not showing loaded data");
				}
			}

			@Override
			public void failure(RetrofitError error) {
				newReleasesTabController.getViewModel().loadingProperty().set(false);
				LOGGER.error("Error loading albums ({})", error.getMessage());
				handleError(error, () -> showNewReleases(genreData));
			}
		};

		if (RHAPSODY_CURATED.equals(genreData.id)) {
			rhapsodySdkWrapper.loadAlbumNewReleases(null, callback);
		} else if (RHAPSODY_PERSONALIZED.equals(genreData.id)) {
			loadPersonalizedNewReleases(callback);
		} else {
			rhapsodySdkWrapper.loadGenreNewReleases(genreData.id, null, callback);
		}
	}

	private void loadPersonalizedNewReleases(Callback<Collection<AlbumData>> callback) {
		if (userAccountData == null) {
			Callback<AccountData> loadUserAccountCallback = new Callback<AccountData>() {

				@Override
				public void success(AccountData userAccountData, Response response) {
					MainController.this.userAccountData = userAccountData;
					rhapsodySdkWrapper.loadAlbumNewReleases(userAccountData.id, callback);
				}

				@Override
				public void failure(RetrofitError error) {
					LOGGER.error("Error loading account information ({})", error.getMessage());
					callback.failure(error);
				}
			};
			rhapsodySdkWrapper.loadAccount(loadUserAccountCallback);
		} else {
			rhapsodySdkWrapper.loadAlbumNewReleases(userAccountData.id, callback);
		}
	}

	public void showArtist(String artistId) {
		ArtistTabViewModel artistTabViewModel = artistTabController.getViewModel();

		artistTabViewModel.loadingProperty().set(true);

		String imageUrl = rhapsodySdkWrapper.getArtistImageUrl(artistId, ArtistImageSize.SIZE_356_237);
		Image image = new Image(imageUrl, true);
		artistTabViewModel.imageProperty().set(image);

		rhapsodySdkWrapper.loadArtistMeta(artistId, new Callback<ArtistData>() {
			@Override
			public void success(ArtistData artistData, Response response) {
				LOGGER.info("Loaded artist '{}'", artistData.name);
				artistTabViewModel.nameProperty().set(artistData.name);
			}

			@Override
			public void failure(RetrofitError error) {
				LOGGER.error("Error loading artist ({})", error.getMessage());
				handleError(error, () -> showArtist(artistId));
			}
		});

		rhapsodySdkWrapper.loadArtistBio(artistId, new Callback<BioData>() {
			@Override
			public void success(BioData bio, Response response) {
				LOGGER.info("Loaded bio, empty: {}, blurbs #: {}", bio.bio.isEmpty(), bio.blurbs.size());
				String blurbs = bio.blurbs.stream().collect(Collectors.joining(",\n"));
				artistTabViewModel.bioProperty().set(bio.bio);
				artistTabViewModel.blubsProperty().set(blurbs);
				artistTabViewModel.loadingProperty().set(false);
			}

			@Override
			public void failure(RetrofitError error) {
				artistTabViewModel.loadingProperty().set(false);
				LOGGER.error("Error loading bio ({})", error.getMessage());
				handleError(error, () -> showArtist(artistId));
			}
		});
	}

	public void showAlbum(String albumId) {
		albumTabController.getViewModel().loadingProperty().set(true);
		rhapsodySdkWrapper.loadAlbum(albumId, new Callback<AlbumData>() {

			@Override
			public void success(AlbumData albumData, Response response) {
				LOGGER.info("Loaded album '{}'", albumData.name);
				albumTabController.getViewModel().setAlbum(albumData);
				albumTabController.getViewModel().loadingProperty().set(false);
			}

			@Override
			public void failure(RetrofitError error) {
				albumTabController.getViewModel().loadingProperty().set(false);
				LOGGER.error("Error loading album ({})", error.getMessage());
				handleError(error, () -> showAlbum(albumId));
			}
		});
	}

	public void clearDetailTabs() {
		artistTabController.getViewModel().clear();
		albumTabController.getViewModel().clear();
	}

	private void showAutoHidingNotification(String icon, String text) {
		String imagePath = MainController.class.getResource("/com/github/kaiwinter/napsterreleases/ui/" + icon).toExternalForm();
		ImageView image = new ImageView(imagePath);
		notificationPane.setGraphic(image);
		notificationPane.setText(text);
		notificationPane.show();

		// Automatically hide
		new Thread(new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
				}
				notificationPane.hide();
				return null;
			}
		}).start();
	}

	/**
	 * Tries to handle the passed <code>error</code> and calls the <code>actionRetryCallback</code> afterwards. If the method don't know how
	 * to handle the error an {@link ExceptionDialog} is shown to the user.
	 *
	 * In case of an 401 error (unauthorized) a token refresh is triggered followed by a username/password login if the former fails. If the
	 * authorization was successful the <code>actionretryCallback</code> is called.
	 *
	 * @param error
	 *            the error to handle
	 * @param actionRetryCallback
	 *            the callback to execute if the error could be solved
	 */
	public void handleError(RetrofitError error, ActionRetryCallback actionRetryCallback) {

		if (error.getResponse() != null && error.getResponse().getStatus() == 401) {
			tryReAuthorization(actionRetryCallback);
		} else {
			Platform.runLater(() -> {
				ExceptionDialog exceptionDialog = new ExceptionDialog(error);
				exceptionDialog.show();
			});
		}
	}

	public void switchToArtistTab() {
		tabPane.getSelectionModel().select(artistTabHandle);
	}

	public void switchToAlbumTab() {
		tabPane.getSelectionModel().select(albumTabHandle);
	}

	private void loadArtistWatchlist() {
		Set<Artist> artists = userSettings.loadWatchedArtists();

		Set<WatchedArtist> watchedArtists = artists.stream().map(artist -> new WatchedArtist(artist)).collect(Collectors.toSet());
		ObservableList<WatchedArtist> sortedList = artistWatchlistTabController.getViewModel().watchedArtists().get();
		ObservableList<WatchedArtist> sourceList = (ObservableList<WatchedArtist>) ((SortedList<WatchedArtist>) sortedList).getSource();
		sourceList.setAll(watchedArtists);

		loadReleaseDates();
	}

	private void loadReleaseDates() {

		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				ObservableList<WatchedArtist> observableWatchedArtists = FXCollections.emptyObservableList();// artistsTv.getItems();
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
					// Platform.runLater(() -> {
					// artistsTv.getColumns().get(2).setVisible(false);
					// artistsTv.getColumns().get(2).setVisible(true);
					// });
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

	public void removeArtistFromWatchlist(WatchedArtist selectedArtist) {
		Set<Artist> watchedArtists = userSettings.loadWatchedArtists();
		watchedArtists = watchedArtists.stream().filter(artist -> !selectedArtist.artist.id.equals(artist.id)).collect(Collectors.toSet());
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

	private void setGenres(Collection<GenreData> genres) {
		Platform.runLater(() -> {
			TreeItem<GenreData> root = new TreeItem<>();
			GenreData rhapsodyDummyGenre = new GenreData();
			rhapsodyDummyGenre.name = "< Rhapsody curated >";
			rhapsodyDummyGenre.id = RHAPSODY_CURATED;
			rhapsodyDummyGenre.description = "Releases curated by Rhapsody.";
			root.getChildren().add(new TreeItem<>(rhapsodyDummyGenre));

			GenreData rhapsodyDummyGenre2 = new GenreData();
			rhapsodyDummyGenre2.name = "< Rhapsody curated, personalized >";
			rhapsodyDummyGenre2.id = RHAPSODY_PERSONALIZED;
			rhapsodyDummyGenre2.description = "Personalized new releases based upon recent listening history.";
			root.getChildren().add(new TreeItem<>(rhapsodyDummyGenre2));

			for (GenreData genreData : genres) {
				TreeItem<GenreData> treeViewItem = new TreeItem<>(genreData);
				root.getChildren().add(treeViewItem);
				if (genreData.subgenres != null) {
					for (GenreData subgenre : genreData.subgenres) {
						treeViewItem.getChildren().add(new TreeItem<>(subgenre));
					}
				}
			}

			newReleasesTabController.getViewModel().genresProperty().set(root);
		});
	}

	private void setNewReleases(Collection<AlbumData> albums) {
		ObservableList<AlbumData> items = (ObservableList<AlbumData>) FilterSupport
				.getUnwrappedList(newReleasesTabController.getViewModel().releasesProperty().get());
		items.setAll(albums);
	}

	private void loadColumnVisibility() {
		// artistTc.setVisible(userSettings.getArtistColumnVisible());
		newReleasesTabController.getViewModel().artistColumVisibleProperty().set(userSettings.isAlbumColumnVisible());
		newReleasesTabController.getViewModel().albumColumVisibleProperty().set(userSettings.isReleasedColumnVisible());
		newReleasesTabController.getViewModel().typeColumVisibleProperty().set(userSettings.isTypeColumnVisible());
		newReleasesTabController.getViewModel().discsColumVisibleProperty().set(userSettings.isDiscColumnVisible());
	}

	private void addColumnVisibilityListeners() {
		NewReleasesTabViewModel viewModel = newReleasesTabController.getViewModel();
		viewModel.artistColumVisibleProperty()
				.addListener((observable, oldValue, newValue) -> userSettings.setArtistColumnVisible(newValue));
		viewModel.albumColumVisibleProperty().addListener((observable, oldValue, newValue) -> userSettings.setAlbumColumnVisible(newValue));
		viewModel.releasedColumVisibleProperty()
				.addListener((observable, oldValue, newValue) -> userSettings.setReleasedColumnVisible(newValue));
		viewModel.typeColumVisibleProperty().addListener((observable, oldValue, newValue) -> userSettings.setTypeColumnVisible(newValue));
		viewModel.discsColumVisibleProperty().addListener((observable, oldValue, newValue) -> userSettings.setDiscColumnVisible(newValue));
	}
}
