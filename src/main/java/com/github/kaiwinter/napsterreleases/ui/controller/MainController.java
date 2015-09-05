package com.github.kaiwinter.napsterreleases.ui.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import org.controlsfx.control.NotificationPane;
import org.controlsfx.dialog.ExceptionDialog;
import org.controlsfx.dialog.LoginDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.napsterreleases.RhapsodyApiKeyConfig;
import com.github.kaiwinter.napsterreleases.ui.callback.ActionRetryCallback;
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
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
	private NewReleasesTabController newReleasesTabController;

	@FXML
	private ArtistTabController artistTabController;

	@FXML
	private AlbumTabController albumTabController;

	@FXML
	private ArtistWatchlistTabController artistWatchlistTabController;

	private RhapsodySdkWrapper rhapsodySdkWrapper;

	private NotificationPane notificationPane;

	private AccountData userAccountData;

	public MainController() throws IOException {
		// Do this in constructor to get a better error output
		RhapsodyApiKeyConfig rhapsodyApiKeyConfig = new RhapsodyApiKeyConfig();
		rhapsodySdkWrapper = new RhapsodySdkWrapper(rhapsodyApiKeyConfig.apiKey, rhapsodyApiKeyConfig.apiSecret,
				new PreferencesAuthorizationStore());
		// rhapsodySdkWrapper.setVerboseLoggingEnabled(true);
	}

	@FXML
	private void initialize() {
		newReleasesTabController.setMainController(this);

		notificationPane = new NotificationPane(borderPane.getCenter());
		notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
		borderPane.setCenter(notificationPane);

		addTabListeners();

		loadGenres();

		artistWatchlistTabController.setRhapsodySdkWrapper(rhapsodySdkWrapper);
		artistWatchlistTabController.loadWatchedArtists();

		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> Platform.runLater(() -> {
			LOGGER.error(throwable.getMessage(), throwable);
			ExceptionDialog exceptionDialog = new ExceptionDialog(throwable);
			exceptionDialog.show();
		}));
	}

	private void addTabListeners() {
		artistTabHandle.setOnSelectionChanged(event -> {
			if (artistTabHandle.isSelected()) {
				AlbumData albumData = newReleasesTabController.getSelectedAlbum();
				if (albumData != null) {
					showArtist(albumData.artist.id);
				}
			}
		});

		albumTabHandle.setOnSelectionChanged(event -> {
			if (albumTabHandle.isSelected()) {
				AlbumData albumData = newReleasesTabController.getSelectedAlbum();
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
		newReleasesTabController.clearData();
		artistTabController.clearData();
		albumTabController.clearData();
	}

	public void loadGenres() {
		newReleasesTabController.setLoading(true);
		rhapsodySdkWrapper.loadGenres(new Callback<Collection<GenreData>>() {

			@Override
			public void success(Collection<GenreData> genres, Response response) {
				LOGGER.info("Loaded {} genres", genres.size());
				newReleasesTabController.setGenres(genres);
				newReleasesTabController.setLoading(false);
			}

			@Override
			public void failure(RetrofitError error) {
				newReleasesTabController.setLoading(false);
				LOGGER.error("Error loading genres ({})", error.getMessage());
				handleError(error, () -> loadGenres());
			}
		});
	}

	public void showNewReleases(GenreData genreData) {
		newReleasesTabController.setLoading(true);
		Callback<Collection<AlbumData>> callback = new Callback<Collection<AlbumData>>() {

			@Override
			public void success(Collection<AlbumData> albums, Response response) {
				LOGGER.info("Loaded {} albums", albums.size());
				// Check if genre selection changed in the meantime
				GenreData currentGenre = newReleasesTabController.getSelectedGenre();
				if (currentGenre != null && currentGenre == genreData) {
					Platform.runLater(() -> {
						clearDetailTabs();
						newReleasesTabController.setNewReleases(albums);
						newReleasesTabController.setLoading(false);
					});
				} else {
					LOGGER.info("Genre selection changed, not showing loaded data");
				}
			}

			@Override
			public void failure(RetrofitError error) {
				newReleasesTabController.setLoading(false);
				LOGGER.error("Error loading albums ({})", error.getMessage());
				handleError(error, () -> showNewReleases(genreData));
			}
		};

		if (NewReleasesTabController.RHAPSODY_CURATED.equals(genreData.id)) {
			rhapsodySdkWrapper.loadAlbumNewReleases(null, callback);
		} else if (NewReleasesTabController.RHAPSODY_PERSONALIZED.equals(genreData.id)) {
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
		artistTabController.setLoading(true);

		String imageUrl = rhapsodySdkWrapper.getArtistImageUrl(artistId, ArtistImageSize.SIZE_356_237);
		Image image = new Image(imageUrl, true);
		artistTabController.setArtistImage(image);

		rhapsodySdkWrapper.loadArtistMeta(artistId, new Callback<ArtistData>() {
			@Override
			public void success(ArtistData artistData, Response response) {
				LOGGER.info("Loaded artist '{}'", artistData.name);
				artistTabController.setArtist(artistData);
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
				artistTabController.setBio(bio);
				artistTabController.setLoading(false);
			}

			@Override
			public void failure(RetrofitError error) {
				artistTabController.setLoading(false);
				LOGGER.error("Error loading bio ({})", error.getMessage());
				handleError(error, () -> showArtist(artistId));
			}
		});
	}

	public void showAlbum(String albumId) {
		albumTabController.setLoading(true);
		rhapsodySdkWrapper.loadAlbum(albumId, new Callback<AlbumData>() {

			@Override
			public void success(AlbumData albumData, Response response) {
				LOGGER.info("Loaded album '{}'", albumData.name);
				albumTabController.setAlbum(albumData);
				albumTabController.setLoading(false);
			}

			@Override
			public void failure(RetrofitError error) {
				albumTabController.setLoading(false);
				LOGGER.error("Error loading album ({})", error.getMessage());
				handleError(error, () -> showAlbum(albumId));
			}
		});
	}

	public void clearDetailTabs() {
		artistTabController.clearData();
		albumTabController.clearData();
	}

	private void showAutoHidingNotification(String icon, String text) {
		String imagePath = MainController.class.getResource("/com/github/kaiwinter/napsterreleases/ui/" + icon).toExternalForm();
		ImageView image = new ImageView(imagePath);
		notificationPane.setGraphic(image);
		notificationPane.getActions().clear();
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

	public void switchToAlbumTab() {
		tabPane.getSelectionModel().select(albumTabHandle);
	}

	public void addArtistToWatchlist(Artist artist) {
		artistWatchlistTabController.addArtistToWatchlist(artist);
	}
}
