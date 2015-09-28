package com.github.kaiwinter.napsterreleases.ui.view;

import java.io.IOException;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.controlsfx.control.NotificationPane;
import org.controlsfx.dialog.LoginDialog;

import com.github.kaiwinter.napsterreleases.ui.NotificationPaneIcon;
import com.github.kaiwinter.napsterreleases.ui.viewmodel.AlbumTabViewModel;
import com.github.kaiwinter.napsterreleases.ui.viewmodel.ArtistTabViewModel;
import com.github.kaiwinter.napsterreleases.ui.viewmodel.MainViewModel;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.util.Pair;

/**
 * Main view of the application window. It contains three more controllers, one for each tab.
 */
@Singleton
public final class MainView implements FxmlView<MainViewModel> {

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

	@FXML
	private Tab libraryTabHandle;

	@FXML
	private LibraryTabView libraryTabController;

	@InjectViewModel
	private MainViewModel mainViewModel;

	@Inject
	private ArtistTabViewModel artistTabViewModel;

	@Inject
	private AlbumTabViewModel albumTabViewModel;

	@FXML
	private void initialize() throws IOException {

		artistTabHandle.setOnSelectionChanged(event -> {
			if (artistTabHandle.isSelected()) {
				artistTabViewModel.showArtist();
			}
		});

		albumTabHandle.setOnSelectionChanged(event -> {
			if (albumTabHandle.isSelected()) {
				albumTabViewModel.showAlbum();
			}
		});

		mainViewModel.bindSelectedAlbumProperty();
	}

	public void switchToArtistTab() {
		tabPane.getSelectionModel().select(artistTabHandle);
	}

	public void switchToAlbumTab() {
		tabPane.getSelectionModel().select(albumTabHandle);
	}

	public void showAutoHidingNotification(NotificationPaneIcon icon, String text) {
		ImageView image = new ImageView(icon.getIconPath());
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

	public Optional<Pair<String, String>> askUserForCredentials() {
		LoginDialog loginDialog = new LoginDialog(null, null);
		loginDialog.initOwner(notificationPane.getScene().getWindow());
		loginDialog.setTitle("Login");
		loginDialog.setHeaderText("Please enter your Rhapsody or Napster login data");
		Optional<Pair<String, String>> userCredentials = loginDialog.showAndWait();
		return userCredentials;
	}

	public boolean askUserToRetry(int status, String reason) {
		ButtonType retry = new ButtonType("Retry", ButtonData.OK_DONE);
		ButtonType[] buttons = { retry, ButtonType.CANCEL };
		Alert dlg = new Alert(AlertType.ERROR, "Login failed. Wrong username/password?", buttons);
		dlg.initModality(Modality.APPLICATION_MODAL);
		dlg.initOwner(notificationPane.getScene().getWindow());

		dlg.setHeaderText(status + " " + reason);
		Optional<ButtonType> selection = dlg.showAndWait();
		if (selection.isPresent()) {
			if (selection.get().getButtonData() == ButtonData.OK_DONE) {
				return true;
			}
		} // else: user canceled
		return false;
	}
}
