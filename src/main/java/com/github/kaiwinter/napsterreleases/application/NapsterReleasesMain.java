package com.github.kaiwinter.napsterreleases.application;

import java.io.IOException;

import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.napsterreleases.ui.view.MainView;
import com.github.kaiwinter.napsterreleases.ui.viewmodel.MainViewModel;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.guice.MvvmfxGuiceApplication;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class which starts the JavaFX application.
 */
public final class NapsterReleasesMain extends MvvmfxGuiceApplication {
   private static final Logger LOGGER = LoggerFactory.getLogger(NapsterReleasesMain.class.getSimpleName());

   public static void main(String... args) throws IOException {
      System.setProperty("proxyHost", "firewall");
      System.setProperty("proxyPort", "3128");

      launch(args);
   }

   @Override
   public void startMvvmfx(Stage primaryStage) throws IOException {

      ViewTuple<MainView, MainViewModel> viewTuple = FluentViewLoader.fxmlView(MainView.class).load();
      Parent root = viewTuple.getView();

      viewTuple.getViewModel().setPrimaryStage(primaryStage);

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