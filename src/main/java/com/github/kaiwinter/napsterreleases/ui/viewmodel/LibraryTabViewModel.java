package com.github.kaiwinter.napsterreleases.ui.viewmodel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Comparator;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.jfx.tablecolumn.filter.FilterSupport;
import com.github.kaiwinter.rhapsody.api.RhapsodyCallback;
import com.github.kaiwinter.rhapsody.model.AlbumData;
import com.github.kaiwinter.rhapsody.model.AlbumData.Artist;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

@Singleton
public final class LibraryTabViewModel implements ViewModel {
   private static final Logger LOGGER = LoggerFactory.getLogger(LibraryTabViewModel.class.getSimpleName());

   private final BooleanProperty loading = new SimpleBooleanProperty();

   private final ListProperty<Artist> artists = new SimpleListProperty<>();
   private final ListProperty<AlbumData> releases = new SimpleListProperty<>();

   private final ObjectProperty<Artist> selectedArtist = new SimpleObjectProperty<>();
   private final ObjectProperty<AlbumData> selectedAlbum = new SimpleObjectProperty<>();

   private final BooleanProperty tabSelected = new SimpleBooleanProperty();

   @Inject
   private SharedViewModel sharedViewModel;

   public BooleanProperty loadingProperty() {
      return this.loading;
   }

   public ListProperty<AlbumData> releasesProperty() {
      return this.releases;
   }

   public ListProperty<Artist> artistsProperty() {
      return this.artists;
   }

   public ObjectProperty<Artist> selectedArtistProperty() {
      return this.selectedArtist;
   }

   public ObjectProperty<AlbumData> selectedAlbumProperty() {
      return this.selectedAlbum;
   }

   public BooleanProperty tabSelectedProperty() {
      return this.tabSelected;
   }

   public void loadAllArtistsInLibrary() {
      artistsProperty().set(FXCollections.observableArrayList());
      loadingProperty().set(true);

      RhapsodyCallback<Collection<Artist>> callback = new RhapsodyCallback<Collection<Artist>>() {

         @Override
         public void onSuccess(Collection<Artist> data) {
            LOGGER.info("Loaded {} artists", data.size());
            ObservableList<Artist> observableArrayList = FXCollections.observableArrayList(data);
            Comparator<Artist> comparator = (o1, o2) -> o1.name.compareTo(o2.name);
            Platform.runLater(() -> artistsProperty().set(observableArrayList.sorted(comparator)));
            loadingProperty().set(false);
         }

         @Override
         public void onFailure(Throwable throwable, int code) {
            LOGGER.error(throwable.getMessage());
            loadingProperty().set(false);
            sharedViewModel.handleError(throwable, code, () -> loadAllArtistsInLibrary());
         }
      };
      sharedViewModel.getRhapsodySdkWrapper().loadAllArtistsInLibrary(null, callback);
   }

   public void loadAlbumsOfSelectedArtist(Artist artist) {
      loadingProperty().set(true);

      RhapsodyCallback<Collection<AlbumData>> callback = new RhapsodyCallback<Collection<AlbumData>>() {

         @Override
         public void onSuccess(Collection<AlbumData> data) {
            LOGGER.info("Loaded {} albums", data.size());
            // Check if selection changed in the meantime
            Artist currentArtist = selectedArtistProperty().get();
            if (currentArtist != null && currentArtist.id.equals(artist.id)) {
               ObservableList<AlbumData> items = (ObservableList<AlbumData>) FilterSupport
                  .getUnwrappedList(releasesProperty().get());
               Platform.runLater(() -> items.setAll(data));
            } else {
               LOGGER.info("Artist selection changed, not showing loaded data");
            }
            loadingProperty().set(false);
         }

         @Override
         public void onFailure(Throwable throwable, int code) {
            LOGGER.error("Error loading albums ({})", throwable.getMessage());
            loadingProperty().set(false);
            sharedViewModel.handleError(throwable, code, () -> loadAlbumsOfSelectedArtist(artist));
         }
      };
      sharedViewModel.getRhapsodySdkWrapper().loadAllAlbumsByArtistInLibrary(artist.id, null, callback);
   }

   public void exportLibrary() {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setInitialFileName("library_export.json");
      fileChooser.getExtensionFilters().add(new ExtensionFilter("JSON File", "*.json"));
      File file = fileChooser.showSaveDialog(sharedViewModel.getPrimaryStage());
      if (file == null) {
         return;
      }

      loadingProperty().set(true);
      RhapsodyCallback<Collection<AlbumData>> rhapsodyCallback = new RhapsodyCallback<Collection<AlbumData>>() {

         @Override
         public void onSuccess(Collection<AlbumData> data) {
            loadingProperty().set(false);

            // reduce data
            data.forEach(e -> {
               e.type = null;
               e.tags = null;
               e.images = null;
               e.discCount = null;
               e.released = null;
            });

            try (FileWriter fileWriter = new FileWriter(file)) {
               Type listOfTestObject = new TypeToken<Collection<AlbumData>>() {
               }.getType();

               Gson gson = new GsonBuilder().setPrettyPrinting().create();
               gson.toJson(data, listOfTestObject, fileWriter);
            } catch (IOException e) {
               LOGGER.error(e.getMessage(), e);
            }
         }

         @Override
         public void onFailure(Throwable throwable, int code) {
            LOGGER.error("Error exporting library ({} {})", code, throwable.getMessage());
            loadingProperty().set(false);
            Platform.runLater(() -> {
               ExceptionDialog exceptionDialog = new ExceptionDialog(throwable);
               exceptionDialog.show();
            });
         }
      };
      sharedViewModel.getRhapsodySdkWrapper().loadAllAlbumsInLibrary(null, rhapsodyCallback);
   }

   public void importLibrary() {
      // FIXME KW Auto-generated method stub

   }

   public void removeArtistFromLibrary(AlbumData albumData) {
      loadingProperty().set(true);

      RhapsodyCallback<Void> callback = new RhapsodyCallback<Void>() {

         @Override
         public void onSuccess(Void data) {
            loadingProperty().set(false);
            loadAlbumsOfSelectedArtist(albumData.artist);
         }

         @Override
         public void onFailure(Throwable throwable, int code) {
            LOGGER.error("Error removing artist from library ({} {})", code, throwable.getMessage());
            loadingProperty().set(false);
            sharedViewModel.handleError(throwable, code, () -> removeArtistFromLibrary(albumData));
         }
      };
      sharedViewModel.getRhapsodySdkWrapper().removeAlbumFromLibrary(albumData.id, callback);
   }

   public void addAlbumToLibrary(AlbumData albumData) {
      RhapsodyCallback<Void> callback = new RhapsodyCallback<Void>() {

         @Override
         public void onSuccess(Void data) {
            loadingProperty().set(false);
            loadAlbumsOfSelectedArtist(albumData.artist);
         }

         @Override
         public void onFailure(Throwable throwable, int code) {
            LOGGER.error("Error adding album to library ({} {})", code, throwable.getMessage());
            loadingProperty().set(false);
            sharedViewModel.handleError(throwable, -1, () -> removeArtistFromLibrary(albumData));
         }
      };
      sharedViewModel.getRhapsodySdkWrapper().addAlbumToLibrary(albumData.id, callback);
   }
}
