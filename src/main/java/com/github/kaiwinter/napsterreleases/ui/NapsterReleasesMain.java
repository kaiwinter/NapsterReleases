package com.github.kaiwinter.napsterreleases.ui;

import java.io.IOException;

import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class which starts the JavaFX application.
 */
public final class NapsterReleasesMain extends Application {
	private static final Logger LOGGER = LoggerFactory.getLogger(NapsterReleasesMain.class.getSimpleName());

	public static void main(String... args) throws IOException {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("ui.fxml"));
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Napster New Releases");
		primaryStage.show();

		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> Platform.runLater(() -> {
			LOGGER.error(throwable.getMessage(), throwable);
			ExceptionDialog exceptionDialog = new ExceptionDialog(throwable);
			exceptionDialog.show();
		}));
	}
}