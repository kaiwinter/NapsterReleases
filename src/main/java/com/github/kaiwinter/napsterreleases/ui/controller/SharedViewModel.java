package com.github.kaiwinter.napsterreleases.ui.controller;

import java.io.IOException;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.controlsfx.dialog.ExceptionDialog;

import com.github.kaiwinter.napsterreleases.RhapsodyApiKeyConfig;
import com.github.kaiwinter.napsterreleases.ui.callback.ActionRetryCallback;
import com.github.kaiwinter.rhapsody.api.AuthenticationCallback;
import com.github.kaiwinter.rhapsody.api.RhapsodySdkWrapper;
import com.github.kaiwinter.rhapsody.persistence.impl.PreferencesAuthorizationStore;

import javafx.application.Platform;
import javafx.util.Pair;
import retrofit.RetrofitError;

/**
 * A View Model which is shared between the other View Models. Most obvious to provide common methods as
 * {@link #handleError(RetrofitError, ActionRetryCallback)} and the ability to initialize the authorization process.
 */
@Singleton
public final class SharedViewModel {

	private final RhapsodySdkWrapper rhapsodySdkWrapper;

	@Inject
	private MainView mainView;

	@Inject
	public SharedViewModel() throws IOException {
		RhapsodyApiKeyConfig rhapsodyApiKeyConfig = new RhapsodyApiKeyConfig();
		rhapsodySdkWrapper = new RhapsodySdkWrapper(rhapsodyApiKeyConfig.apiKey, rhapsodyApiKeyConfig.apiSecret,
				new PreferencesAuthorizationStore());
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
					mainView.showAutoHidingNotification("notification-pane-warning.png",
							"Authentication failed (" + status + ") - " + reason);

					authorize(actionCallback);
				});
			}
		});
	}

	private void authorize(ActionRetryCallback actionCallback) {
		Optional<Pair<String, String>> userCredentials = mainView.askUserForCredentials();
		if (!userCredentials.isPresent()) {
			return;
		}

		Pair<String, String> pair = userCredentials.get();
		rhapsodySdkWrapper.authorize(pair.getKey(), pair.getValue(), new AuthenticationCallback() {

			@Override
			public void success() {
				Platform.runLater(() -> {
					mainView.showAutoHidingNotification("notification-pane-info.png", "Authentication successful");

					actionCallback.retryAction();
				});
			}

			@Override
			public void failure(int status, String reason) {
				Platform.runLater(() -> {
					boolean retry = mainView.askUserToRetry(status, reason);
					if (retry) {
						authorize(actionCallback);
					}
				});
			}
		});
	}

	public void logout() {
		rhapsodySdkWrapper.clearAuthorization();
	}

	public RhapsodySdkWrapper getRhapsodySdkWrapper() {
		return rhapsodySdkWrapper;
	}
}
