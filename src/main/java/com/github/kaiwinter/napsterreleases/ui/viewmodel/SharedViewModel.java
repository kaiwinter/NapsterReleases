package com.github.kaiwinter.napsterreleases.ui.viewmodel;

import java.io.IOException;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.controlsfx.dialog.ExceptionDialog;

import com.github.kaiwinter.napsterreleases.persistence.RhapsodyApiKeyProperties;
import com.github.kaiwinter.napsterreleases.ui.NotificationPaneIcon;
import com.github.kaiwinter.napsterreleases.ui.view.MainView;
import com.github.kaiwinter.rhapsody.api.AuthenticationCallback;
import com.github.kaiwinter.rhapsody.api.RhapsodySdkWrapper;
import com.github.kaiwinter.rhapsody.persistence.impl.PreferencesAuthorizationStore;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Window;
import javafx.util.Pair;

/**
 * A View Model which is shared between the other View Models. Most obvious to provide common methods as
 * {@link #handleError(RetrofitError, ActionRetryCallback)} and the ability to initialize the authorization process.
 */
@Singleton
public class SharedViewModel {

   private final RhapsodySdkWrapper rhapsodySdkWrapper;

   @Inject
   private MainView mainView;

   @Inject
   private MainViewModel mainViewModel;

   public SharedViewModel() throws IOException {
      RhapsodyApiKeyProperties rhapsodyApiKeyConfig = new RhapsodyApiKeyProperties();
      rhapsodySdkWrapper = new RhapsodySdkWrapper(rhapsodyApiKeyConfig.apiKey, rhapsodyApiKeyConfig.apiSecret,
         new PreferencesAuthorizationStore());
      rhapsodySdkWrapper.setVerboseLoggingEnabled(true);
   }

   /**
    * <p>
    * Tries to handle the passed <code>error</code> and calls the <code>actionRetryCallback</code> afterwards. If the
    * method don't know how to handle the error an {@link ExceptionDialog} is shown to the user.
    * </p>
    * <p>
    * In case of an 401 error (unauthorized) a token refresh is triggered followed by a username/password login if the
    * former fails. If the authorization was successful the <code>actionretryCallback</code> is called.
    * </p>
    * 
    * @param message
    *           the message to show
    * @param actionRetryCallback
    *           the callback to execute if the error could be solved
    */
   public void handleError(String message, int httpCode, ActionRetryCallback actionRetryCallback) {

      if (httpCode == 401) {
         tryReAuthorization(actionRetryCallback);
      } else {
         Platform.runLater(() -> {
            Alert exceptionDialog = new Alert(AlertType.ERROR, message);
            exceptionDialog.show();
         });
      }
   }

   /**
    * Tries to re-authenticate by using the refresh_token to get a new access_token. If this fails (or no refresh_token
    * is available) the user gets asked for his credentials.
    *
    * @param actionCallback
    *           action to execute if re-authentication succeeds
    */
   private void tryReAuthorization(ActionRetryCallback actionCallback) {
      rhapsodySdkWrapper.refreshToken(new AuthenticationCallback() {
         @Override
         public void success() {
            actionCallback.retryAction();
         }

         @Override
         public void failure(int status, String reason) {
            showAutoHidingNotification(NotificationPaneIcon.WARNING,
               "Authentication failed (" + status + ") - " + reason);

            Platform.runLater(() -> {
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
            showAutoHidingNotification(NotificationPaneIcon.INFO, "Authentication successful");

            Platform.runLater(() -> {
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

   public void showAutoHidingNotification(NotificationPaneIcon icon, String message) {
      Platform.runLater(() -> mainView.showAutoHidingNotification(icon, message));
   }

   public Window getPrimaryStage() {
      return mainViewModel.getPrimarySage();
   }
}
