package com.github.kaiwinter.napsterreleases.ui.viewmodel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
      Platform.runLater(() -> artistsProperty().set(FXCollections.observableArrayList()));
      loadingProperty().set(true);
      RhapsodyCallback<Collection<Artist>> callback = new RhapsodyCallback<Collection<Artist>>() {
         @Override
         public void onSuccess(Collection<Artist> artists) {
            LOGGER.info("Loaded {} artists", artists.size());
            ObservableList<Artist> observableArrayList = FXCollections.observableArrayList(artists);
            Comparator<Artist> comparator = (o1, o2) -> o1.name.compareTo(o2.name);
            Platform.runLater(() -> artistsProperty().set(observableArrayList.sorted(comparator)));
            loadingProperty().set(false);
         }

         @Override
         public void onFailure(int httpCode, String message) {
            LOGGER.error("{} {}", httpCode, message);
            loadingProperty().set(false);
            sharedViewModel.handleError(httpCode, message, () -> loadAllArtistsInLibrary());
         }
      };
      sharedViewModel.getRhapsodySdkWrapper().loadAllArtistsInLibrary(null, callback);
   }

   public void loadAlbumsOfSelectedArtist(Artist artist) {
      loadingProperty().set(true);
      RhapsodyCallback<Collection<AlbumData>> callback = new RhapsodyCallback<Collection<AlbumData>>() {
         @Override
         public void onSuccess(Collection<AlbumData> albums) {
            LOGGER.info("Loaded {} albums", albums.size());
            // Check if selection changed in the meantime
            Artist currentArtist = selectedArtistProperty().get();
            if (currentArtist != null && currentArtist.id.equals(artist.id)) {
               ObservableList<AlbumData> items = (ObservableList<AlbumData>) FilterSupport
                  .getUnwrappedList(releasesProperty().get());
               Platform.runLater(() -> items.setAll(albums));
            } else {
               LOGGER.info("Artist selection changed, not showing loaded data");
            }
            loadingProperty().set(false);
         }

         @Override
         public void onFailure(int httpCode, String message) {
            LOGGER.error("{} {}", httpCode, message);
            loadingProperty().set(false);
            sharedViewModel.handleError(httpCode, message, () -> loadAlbumsOfSelectedArtist(artist));
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
      RhapsodyCallback<Collection<AlbumData>> callback = new RhapsodyCallback<Collection<AlbumData>>() {
         @Override
         public void onSuccess(Collection<AlbumData> albums) {
            loadingProperty().set(false);

            // reduce data
            albums.forEach(e -> {
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
               gson.toJson(albums, listOfTestObject, fileWriter);
            } catch (IOException e) {
               LOGGER.error(e.getMessage(), e);
            }
         }

         @Override
         public void onFailure(int httpCode, String message) {
            LOGGER.error("{} {}", httpCode, message);
            loadingProperty().set(false);
            Platform.runLater(() -> {
               Alert exceptionDialog = new Alert(AlertType.ERROR, message);
               exceptionDialog.show();
            });
         }
      };
      sharedViewModel.getRhapsodySdkWrapper().loadAllAlbumsInLibrary(null, callback);
   }

   public void importLibrary() {
      // FIXME KW Auto-generated method stub

   }

   public void removeAlbumFromLibrary(AlbumData albumData) {
      loadingProperty().set(true);
      RhapsodyCallback<Void> callback = new RhapsodyCallback<Void>() {
         @Override
         public void onSuccess(Void data) {
            loadingProperty().set(false);
            loadAlbumsOfSelectedArtist(albumData.artist);
         }

         @Override
         public void onFailure(int httpCode, String message) {
            LOGGER.error("{} {}", httpCode, message);
            loadingProperty().set(false);
            sharedViewModel.handleError(httpCode, message, () -> removeAlbumFromLibrary(albumData));
         }
      };
      sharedViewModel.getRhapsodySdkWrapper().removeAlbumFromLibrary(albumData.id, callback);
   }

   /**
    * Removes all albums of the artist from the library. First the albums of the artist are loaded from the library and
    * afterwards deleted.
    *
    * @param artist
    *           the artist to remove
    */
   public void removeArtistFromLibrary(Artist artist) {
      loadingProperty().set(true);

      AtomicInteger threadCount = new AtomicInteger();
      RhapsodyCallback<Void> removeAlbumCallback = new RhapsodyCallback<Void>() {
         @Override
         public void onSuccess(Void data) {
            int runningThreads = threadCount.decrementAndGet();
            if (runningThreads == 0) {
               LOGGER.info("Last album removed, refreshing view");
               loadAllArtistsInLibrary();
            }
         }

         @Override
         public void onFailure(int httpCode, String message) {
            LOGGER.error("{} {}", httpCode, message);
            sharedViewModel.handleError(httpCode, message, () -> removeArtistFromLibrary(artist));
         }
      };

      RhapsodyCallback<Collection<AlbumData>> loadAlbumsCallback = new RhapsodyCallback<Collection<AlbumData>>() {
         @Override
         public void onSuccess(Collection<AlbumData> albums) {
            LOGGER.info("Loaded {} albums", albums.size());

            for (AlbumData albumData : albums) {
               threadCount.incrementAndGet();
               sharedViewModel.getRhapsodySdkWrapper().removeAlbumFromLibrary(albumData.id, removeAlbumCallback);
            }
            loadingProperty().set(false);
         }

         @Override
         public void onFailure(int httpCode, String message) {
            LOGGER.error("{} {}", httpCode, message);
            loadingProperty().set(false);
            sharedViewModel.handleError(httpCode, message, () -> removeArtistFromLibrary(artist));
         }
      };
      sharedViewModel.getRhapsodySdkWrapper().loadAllAlbumsByArtistInLibrary(artist.id, null, loadAlbumsCallback);
   }

   public void addAlbumToLibrary(AlbumData albumData) {
      RhapsodyCallback<Void> callback = new RhapsodyCallback<Void>() {
         @Override
         public void onSuccess(Void data) {
            loadingProperty().set(false);
            loadAlbumsOfSelectedArtist(albumData.artist);
         }

         @Override
         public void onFailure(int httpCode, String message) {
            LOGGER.error("{} {}", httpCode, message);
            loadingProperty().set(false);
            sharedViewModel.handleError(httpCode, message, () -> removeAlbumFromLibrary(albumData));
         }
      };
      sharedViewModel.getRhapsodySdkWrapper().addAlbumToLibrary(albumData.id, callback);
   }
}
