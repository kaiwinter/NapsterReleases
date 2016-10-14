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

import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.jfx.tablecolumn.filter.FilterSupport;
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
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

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
      Callback<Collection<Artist>> callback = new Callback<Collection<Artist>>() {

         @Override
         public void success(Collection<Artist> artists, Response response) {
            LOGGER.info("Loaded {} artists", artists.size());
            ObservableList<Artist> observableArrayList = FXCollections.observableArrayList(artists);
            Comparator<Artist> comparator = (o1, o2) -> o1.name.compareTo(o2.name);
            Platform.runLater(() -> artistsProperty().set(observableArrayList.sorted(comparator)));
            loadingProperty().set(false);
         }

         @Override
         public void failure(RetrofitError error) {
            LOGGER.error(error.getMessage(), error);
            loadingProperty().set(false);
            sharedViewModel.handleError(error, () -> loadAllArtistsInLibrary());
         }
      };
      sharedViewModel.getRhapsodySdkWrapper().loadAllArtistsInLibrary(null, callback);
   }

   public void loadAlbumsOfSelectedArtist(Artist artist) {
      loadingProperty().set(true);
      Callback<Collection<AlbumData>> callback = new Callback<Collection<AlbumData>>() {

         @Override
         public void success(Collection<AlbumData> albums, Response response) {
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
         public void failure(RetrofitError error) {
            LOGGER.error(error.getMessage(), error);
            loadingProperty().set(false);
            sharedViewModel.handleError(error, () -> loadAlbumsOfSelectedArtist(artist));
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
      Callback<Collection<AlbumData>> callback = new Callback<Collection<AlbumData>>() {
         @Override
         public void success(Collection<AlbumData> albums, Response response) {
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
         public void failure(RetrofitError error) {
            LOGGER.error(error.getMessage(), error);
            loadingProperty().set(false);
            Platform.runLater(() -> {
               ExceptionDialog exceptionDialog = new ExceptionDialog(error);
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
      Callback<Void> callback = new Callback<Void>() {

         @Override
         public void success(Void t, Response response) {
            loadingProperty().set(false);
            loadAlbumsOfSelectedArtist(albumData.artist);
         }

         @Override
         public void failure(RetrofitError error) {
            LOGGER.error(error.getMessage(), error);
            loadingProperty().set(false);
            sharedViewModel.handleError(error, () -> removeAlbumFromLibrary(albumData));
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
      Callback<Void> removeAlbumCallback = new Callback<Void>() {

         @Override
         public void success(Void empty, Response response) {
            int runningThreads = threadCount.decrementAndGet();
            if (runningThreads == 0) {
               LOGGER.info("Last album removed, refreshing view");
               loadAllArtistsInLibrary();
            }
         }

         @Override
         public void failure(RetrofitError error) {
            LOGGER.error(error.getMessage(), error);
            sharedViewModel.handleError(error, () -> removeArtistFromLibrary(artist));
         }
      };

      Callback<Collection<AlbumData>> loadAlbumsCallback = new Callback<Collection<AlbumData>>() {

         @Override
         public void success(Collection<AlbumData> albums, Response response) {
            LOGGER.info("Loaded {} albums", albums.size());

            for (AlbumData albumData : albums) {
               threadCount.incrementAndGet();
               sharedViewModel.getRhapsodySdkWrapper().removeAlbumFromLibrary(albumData.id, removeAlbumCallback);
            }
            loadingProperty().set(false);
         }

         @Override
         public void failure(RetrofitError error) {
            LOGGER.error(error.getMessage(), error);
            loadingProperty().set(false);
            sharedViewModel.handleError(error, () -> removeArtistFromLibrary(artist));
         }
      };
      sharedViewModel.getRhapsodySdkWrapper().loadAllAlbumsByArtistInLibrary(artist.id, null, loadAlbumsCallback);
   }

   public void addAlbumToLibrary(AlbumData albumData) {
      Callback<Void> callback = new Callback<Void>() {

         @Override
         public void success(Void t, Response response) {
            loadingProperty().set(false);
            loadAlbumsOfSelectedArtist(albumData.artist);
         }

         @Override
         public void failure(RetrofitError error) {
            LOGGER.error(error.getMessage(), error);
            loadingProperty().set(false);
            sharedViewModel.handleError(error, () -> removeAlbumFromLibrary(albumData));
         }
      };
      sharedViewModel.getRhapsodySdkWrapper().addAlbumToLibrary(albumData.id, callback);
   }
}
