package com.github.kaiwinter.napsterreleases.ui.viewmodel;

import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.rhapsody.api.ArtistImageSize;
import com.github.kaiwinter.rhapsody.api.RhapsodyCallback;
import com.github.kaiwinter.rhapsody.model.AlbumData;
import com.github.kaiwinter.rhapsody.model.ArtistData;
import com.github.kaiwinter.rhapsody.model.BioData;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.image.Image;

@Singleton
public final class ArtistTabViewModel implements ViewModel {
   private static final Logger LOGGER = LoggerFactory.getLogger(ArtistTabViewModel.class.getSimpleName());

   private final BooleanProperty loading = new SimpleBooleanProperty();

   private final StringProperty name = new SimpleStringProperty();
   private final StringProperty bio = new SimpleStringProperty();
   private final StringProperty blubs = new SimpleStringProperty();
   private final ObjectProperty<Image> image = new SimpleObjectProperty<>();
   private final ObjectProperty<AlbumData> selectedAlbum = new SimpleObjectProperty<AlbumData>();

   @Inject
   private SharedViewModel sharedViewModel;

   public ArtistTabViewModel() {
      selectedAlbum.addListener((ChangeListener<AlbumData>) (observable, oldValue, newValue) -> clear());
   }

   public BooleanProperty loadingProperty() {
      return this.loading;
   }

   public StringProperty nameProperty() {
      return this.name;
   }

   public StringProperty bioProperty() {
      return this.bio;
   }

   public StringProperty blubsProperty() {
      return this.blubs;
   }

   public ObjectProperty<Image> imageProperty() {
      return this.image;
   }

   public ObjectProperty<AlbumData> selectedAlbumProperty() {
      return this.selectedAlbum;
   }

   private void clear() {
      name.setValue(null);
      bio.setValue(null);
      blubs.setValue(null);
      image.setValue(null);
   }

   public void showArtist() {
      AlbumData albumData = selectedAlbum.get();
      if (albumData == null) {
         return;
      }
      String artistId = albumData.artist.id;
      loadingProperty().set(true);

      String imageUrl = sharedViewModel.getRhapsodySdkWrapper().getArtistImageUrl(artistId,
         ArtistImageSize.SIZE_356_237);
      Image image = new Image(imageUrl, true);
      imageProperty().set(image);

      sharedViewModel.getRhapsodySdkWrapper().loadArtistMeta(artistId, new RhapsodyCallback<ArtistData>() {
         @Override
         public void onSuccess(ArtistData artistData) {
            LOGGER.info("Loaded artist '{}'", artistData.name);
            nameProperty().set(artistData.name);
         }

         @Override
         public void onFailure(int httpCode, String message) {
            LOGGER.error("Error loading artist ({})", message);
            sharedViewModel.handleError(httpCode, message, () -> showArtist());
         }
      });

      sharedViewModel.getRhapsodySdkWrapper().loadArtistBio(artistId, new RhapsodyCallback<BioData>() {
         @Override
         public void onSuccess(BioData bioData) {
            LOGGER.info("Loaded bio, empty: {}, blurbs #: {}", bioData.bio.isEmpty(), bioData.blurbs.size());
            String blurbs = bioData.blurbs.stream().collect(Collectors.joining(",\n"));
            bioProperty().set(bioData.bio);
            blubsProperty().set(blurbs);
            loadingProperty().set(false);
         }

         @Override
         public void onFailure(int httpCode, String message) {
            loadingProperty().set(false);
            LOGGER.error("Error loading bio ({})", message);
            sharedViewModel.handleError(httpCode, message, () -> showArtist());
         }
      });
   }
}
