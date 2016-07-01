package com.github.kaiwinter.napsterreleases.ui.viewmodel;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.jfx.tablecolumn.filter.FilterSupport;
import com.github.kaiwinter.napsterreleases.persistence.UISettings;
import com.github.kaiwinter.rhapsody.api.RhapsodyCallback;
import com.github.kaiwinter.rhapsody.model.AccountData;
import com.github.kaiwinter.rhapsody.model.AlbumData;
import com.github.kaiwinter.rhapsody.model.GenreData;

import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

@Singleton
public final class NewReleasesTabViewModel implements ViewModel {
   private static final Logger LOGGER = LoggerFactory.getLogger(NewReleasesTabViewModel.class.getSimpleName());

   private static final String RHAPSODY_CURATED = "rhapsody_curated";
   private static final String RHAPSODY_PERSONALIZED = "rhapsody_personalized";

   private final BooleanProperty loading = new SimpleBooleanProperty();

   private final ObjectProperty<TreeItem<GenreData>> genres = new SimpleObjectProperty<>();
   private final ListProperty<AlbumData> releases = new SimpleListProperty<>();
   private final StringProperty genreDescription = new SimpleStringProperty();

   private final ObjectProperty<TreeItem<GenreData>> selectedGenre = new SimpleObjectProperty<>();
   private final ObjectProperty<AlbumData> selectedAlbum = new SimpleObjectProperty<>();

   private final BooleanProperty artistColumVisible = new SimpleBooleanProperty();
   private final BooleanProperty albumColumVisible = new SimpleBooleanProperty();
   private final BooleanProperty releasedColumVisible = new SimpleBooleanProperty();
   private final BooleanProperty typeColumVisible = new SimpleBooleanProperty();
   private final BooleanProperty discsColumVisible = new SimpleBooleanProperty();

   private final BooleanProperty tabSelected = new SimpleBooleanProperty();

   @Inject
   private SharedViewModel sharedViewModel;

   @Inject
   private UISettings userSettings;

   private AccountData userAccountData;

   public BooleanProperty loadingProperty() {
      return this.loading;
   }

   public ObjectProperty<TreeItem<GenreData>> genresProperty() {
      return this.genres;
   }

   public ListProperty<AlbumData> releasesProperty() {
      return this.releases;
   }

   public StringProperty genreDescriptionProperty() {
      return this.genreDescription;
   }

   public ObjectProperty<TreeItem<GenreData>> selectedGenreProperty() {
      return this.selectedGenre;
   }

   public ObjectProperty<AlbumData> selectedAlbumProperty() {
      return this.selectedAlbum;
   }

   public BooleanProperty artistColumVisibleProperty() {
      return this.artistColumVisible;
   }

   public BooleanProperty albumColumVisibleProperty() {
      return this.albumColumVisible;
   }

   public BooleanProperty releasedColumVisibleProperty() {
      return this.releasedColumVisible;
   }

   public BooleanProperty typeColumVisibleProperty() {
      return this.typeColumVisible;
   }

   public BooleanProperty discsColumVisibleProperty() {
      return this.discsColumVisible;
   }

   public BooleanProperty tabSelectedProperty() {
      return this.tabSelected;
   }

   private void clearData() {
      genres.set(null);
      FilterSupport.getUnwrappedList(releases.get()).clear();
      genreDescription.set(null);
   }

   public void loadGenres() {
      clearData();
      loadingProperty().set(true);

      RhapsodyCallback<Collection<GenreData>> rhapsodyCallback = new RhapsodyCallback<Collection<GenreData>>() {

         @Override
         public void onSuccess(Collection<GenreData> data) {
            LOGGER.info("Loaded {} genres", data.size());
            setGenres(data);
            loadingProperty().set(false);
         }

         @Override
         public void onFailure(Throwable throwable, int code) {
            LOGGER.error("Error loading genres ({} {})", code, throwable.getMessage());
            sharedViewModel.handleError(throwable, code, () -> loadGenres());
         }
      };
      sharedViewModel.getRhapsodySdkWrapper().loadGenres(rhapsodyCallback);
   }

   private void setGenres(Collection<GenreData> genres) {
      Platform.runLater(() -> {
         GenreData rhapsodyDummyGenre = new GenreData();
         rhapsodyDummyGenre.name = "< Rhapsody curated >";
         rhapsodyDummyGenre.id = RHAPSODY_CURATED;
         rhapsodyDummyGenre.description = "Releases curated by Rhapsody.";
         TreeItem<GenreData> root = new TreeItem<>();
         root.getChildren().add(new TreeItem<>(rhapsodyDummyGenre));

         GenreData rhapsodyDummyGenre2 = new GenreData();
         rhapsodyDummyGenre2.name = "< Rhapsody curated, personalized >";
         rhapsodyDummyGenre2.id = RHAPSODY_PERSONALIZED;
         rhapsodyDummyGenre2.description = "Personalized new releases based upon recent listening history.";
         root.getChildren().add(new TreeItem<>(rhapsodyDummyGenre2));

         for (GenreData genreData : genres) {
            TreeItem<GenreData> treeViewItem = new TreeItem<>(genreData);
            root.getChildren().add(treeViewItem);
            if (genreData.subgenres != null) {
               for (GenreData subgenre : genreData.subgenres) {
                  treeViewItem.getChildren().add(new TreeItem<>(subgenre));
               }
            }
         }

         genresProperty().set(root);
      });
   }

   public void showNewReleases(GenreData genreData) {
      loadingProperty().set(true);

      RhapsodyCallback<Collection<AlbumData>> rhapsodyCallback = new RhapsodyCallback<Collection<AlbumData>>() {

         @Override
         public void onSuccess(Collection<AlbumData> data) {
            LOGGER.info("Loaded {} albums", data.size());
            // Check if genre selection changed in the meantime
            GenreData currentGenre = selectedGenreProperty().get().getValue();
            if (currentGenre != null && currentGenre == genreData) {
               @SuppressWarnings("unchecked")
               ObservableList<AlbumData> items = (ObservableList<AlbumData>) FilterSupport
                  .getUnwrappedList(releasesProperty().get());
               Platform.runLater(() -> {
                  items.setAll(data);
               });
               loadingProperty().set(false);
            } else {
               LOGGER.info("Genre selection changed, not showing loaded data");
            }
         }

         @Override
         public void onFailure(Throwable throwable, int code) {
            loadingProperty().set(false);
            LOGGER.error("Error loading albums ({} {})", code, throwable.getMessage());
            sharedViewModel.handleError(throwable, code, () -> showNewReleases(genreData));
         }
      };

      if (RHAPSODY_CURATED.equals(genreData.id)) {
         sharedViewModel.getRhapsodySdkWrapper().loadAlbumNewReleases(null, rhapsodyCallback);
      } else if (RHAPSODY_PERSONALIZED.equals(genreData.id)) {
         loadPersonalizedNewReleases(rhapsodyCallback);
      } else {
         sharedViewModel.getRhapsodySdkWrapper().loadGenreNewReleases(genreData.id, null, rhapsodyCallback);
      }
   }

   private void loadPersonalizedNewReleases(RhapsodyCallback<Collection<AlbumData>> callback) {
      if (userAccountData == null) {
         RhapsodyCallback<AccountData> rhapsodyCallbackAccountData = new RhapsodyCallback<AccountData>() {

            @Override
            public void onSuccess(AccountData data) {
               NewReleasesTabViewModel.this.userAccountData = userAccountData;
               sharedViewModel.getRhapsodySdkWrapper().loadAlbumNewReleases(userAccountData.id, callback);
            }

            @Override
            public void onFailure(Throwable throwable, int code) {
               LOGGER.error("Error loading account information ({} {})", code, throwable.getMessage());
               callback.onFailure(throwable, code);
            }
         };

         sharedViewModel.getRhapsodySdkWrapper().loadAccount(rhapsodyCallbackAccountData);
      } else {
         sharedViewModel.getRhapsodySdkWrapper().loadAlbumNewReleases(userAccountData.id, callback);
      }
   }

   public void loadColumnVisibility() {
      // artistTc.setVisible(userSettings.getArtistColumnVisible());
      artistColumVisibleProperty().set(userSettings.isArtistColumnVisible());
      albumColumVisibleProperty().set(userSettings.isAlbumColumnVisible());
      releasedColumVisibleProperty().set(userSettings.isReleasedColumnVisible());
      typeColumVisibleProperty().set(userSettings.isTypeColumnVisible());
      discsColumVisibleProperty().set(userSettings.isDiscColumnVisible());
   }

   public void addColumnVisibilityListeners() {
      artistColumVisibleProperty()
         .addListener((observable, oldValue, newValue) -> userSettings.setArtistColumnVisible(newValue));
      albumColumVisibleProperty()
         .addListener((observable, oldValue, newValue) -> userSettings.setAlbumColumnVisible(newValue));
      releasedColumVisibleProperty()
         .addListener((observable, oldValue, newValue) -> userSettings.setReleasedColumnVisible(newValue));
      typeColumVisibleProperty()
         .addListener((observable, oldValue, newValue) -> userSettings.setTypeColumnVisible(newValue));
      discsColumVisibleProperty()
         .addListener((observable, oldValue, newValue) -> userSettings.setDiscColumnVisible(newValue));
   }

   public void logout() {
      sharedViewModel.logout();
      clearData();
   }
}
