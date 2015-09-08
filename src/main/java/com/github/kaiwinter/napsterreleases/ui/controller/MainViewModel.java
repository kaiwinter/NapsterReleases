package com.github.kaiwinter.napsterreleases.ui.controller;

import java.io.IOException;
import java.util.Optional;

import org.controlsfx.dialog.ExceptionDialog;

import com.github.kaiwinter.napsterreleases.RhapsodyApiKeyConfig;
import com.github.kaiwinter.napsterreleases.UserSettings;
import com.github.kaiwinter.napsterreleases.ui.callback.ActionRetryCallback;
import com.github.kaiwinter.rhapsody.api.AuthenticationCallback;
import com.github.kaiwinter.rhapsody.api.RhapsodySdkWrapper;
import com.github.kaiwinter.rhapsody.model.AlbumData.Artist;
import com.github.kaiwinter.rhapsody.persistence.impl.PreferencesAuthorizationStore;

import javafx.application.Platform;
import javafx.util.Pair;
import retrofit.RetrofitError;

public final class MainViewModel {

	private final MainView mainController;

	private final RhapsodySdkWrapper rhapsodySdkWrapper;
	private final UserSettings userSettings;

	private final NewReleasesTabViewModel newReleasesTabViewModel;
	private final ArtistTabViewModel artistTabViewModel;
	private final AlbumTabViewModel albumTabViewModel;
	private final ArtistWatchlistTabViewModel artistWatchlistTabViewModel;

	public MainViewModel(MainView mainController, NewReleasesTabView newReleasesTabController, ArtistTabView artistTabController,
			AlbumTabView albumTabController, ArtistWatchlistTabView artistWatchlistTabController) throws IOException {
		this.mainController = mainController;
		this.userSettings = new UserSettings();

		RhapsodyApiKeyConfig rhapsodyApiKeyConfig = new RhapsodyApiKeyConfig();
		rhapsodySdkWrapper = new RhapsodySdkWrapper(rhapsodyApiKeyConfig.apiKey, rhapsodyApiKeyConfig.apiSecret,
				new PreferencesAuthorizationStore());

		// Instantiate ViewModels
		newReleasesTabViewModel = new NewReleasesTabViewModel(rhapsodySdkWrapper, userSettings);
		albumTabViewModel = new AlbumTabViewModel(rhapsodySdkWrapper);
		artistTabViewModel = new ArtistTabViewModel(rhapsodySdkWrapper);
		artistWatchlistTabViewModel = new ArtistWatchlistTabViewModel(rhapsodySdkWrapper, userSettings);

		// Set ViewModels in Views
		newReleasesTabController.setViewModel(newReleasesTabViewModel);
		artistTabController.setViewModel(artistTabViewModel);
		albumTabController.setViewModel(albumTabViewModel);
		artistWatchlistTabController.setViewModel(artistWatchlistTabViewModel);

		newReleasesTabController.setMainViewModel(this);

		newReleasesTabViewModel.setMainViewModel(this);
		albumTabViewModel.setMainViewModel(this);
		artistTabViewModel.setMainViewModel(this);

		artistTabViewModel.selectedAlbumProperty().bind(newReleasesTabViewModel.selectedAlbumProperty());
		albumTabViewModel.selectedAlbumProperty().bind(newReleasesTabViewModel.selectedAlbumProperty());

		newReleasesTabViewModel.loadGenres();

		newReleasesTabViewModel.loadColumnVisibility();
		newReleasesTabViewModel.addColumnVisibilityListeners();

		artistWatchlistTabViewModel.loadArtistWatchlist();
	}

	/**
	 * Tries to re-authenticate by using the refresh_token to get a new access_token. If this fails (or no refresh_token is available) the
	 * user gets asked for his credentials.
	 *
	 * @param actionCallback
	 *            action to execute if re-authentication succeeds
	 */
	private void tryReAuthorization(ActionRetryCallback actionCallback) {
		rhapsodySdkWrapper.refreshToken(new AuthenticationCallback() {
			@Override
			public void success() {
				actionCallback.retryAction();
			}

			@Override
			public void failure(int status, String reason) {
				Platform.runLater(() -> {
					mainController.showAutoHidingNotification("notification-pane-warning.png",
							"Authentication failed (" + status + ") - " + reason);

					authorize(actionCallback);
				});
			}
		});
	}

	private void authorize(ActionRetryCallback actionCallback) {
		Optional<Pair<String, String>> userCredentials = mainController.askUserForCredentials();
		if (!userCredentials.isPresent()) {
			return;
		}

		Pair<String, String> pair = userCredentials.get();
		rhapsodySdkWrapper.authorize(pair.getKey(), pair.getValue(), new AuthenticationCallback() {

			@Override
			public void success() {
				Platform.runLater(() -> {
					mainController.showAutoHidingNotification("notification-pane-info.png", "Authentication successful");

					actionCallback.retryAction();
				});
			}

			@Override
			public void failure(int status, String reason) {
				Platform.runLater(() -> {
					boolean retry = mainController.askUserToRetry(status, reason);
					if (retry) {
						authorize(actionCallback);
					}
				});
			}
		});
	}

	public void logout() {
		rhapsodySdkWrapper.clearAuthorization();
		newReleasesTabViewModel.clearData();
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
