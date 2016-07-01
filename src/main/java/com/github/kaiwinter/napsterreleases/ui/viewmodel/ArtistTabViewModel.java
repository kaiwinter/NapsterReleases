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

      RhapsodyCallback<ArtistData> artistCallback = new RhapsodyCallback<ArtistData>() {

         @Override
         public void onSuccess(ArtistData data) {
            LOGGER.info("Loaded artist '{}'", data.name);
            nameProperty().set(data.name);
         }

         @Override
         public void onFailure(Throwable throwable, int code) {
            LOGGER.error("Error loading artist ({} {})", code, throwable.getMessage());
            sharedViewModel.handleError(throwable, code, () -> showArtist());
         }
      };

      sharedViewModel.getRhapsodySdkWrapper().loadArtistMeta(artistId, artistCallback);

      RhapsodyCallback<BioData> bioCallback = new RhapsodyCallback<BioData>() {

         @Override
         public void onSuccess(BioData data) {
            LOGGER.info("Loaded bio, empty: {}, blurbs #: {}", data.bio.isEmpty(), data.blurbs.size());
            String blurbs = data.blurbs.stream().collect(Collectors.joining(",\n"));
            bioProperty().set(data.bio);
            blubsProperty().set(blurbs);
            loadingProperty().set(false);
         }

         @Override
         public void onFailure(Throwable throwable, int code) {
            loadingProperty().set(false);
            LOGGER.error("Error loading bio ({} {})", code, throwable.getMessage());
            sharedViewModel.handleError(throwable, code, () -> showArtist());
         }
      };
      sharedViewModel.getRhapsodySdkWrapper().loadArtistBio(artistId, bioCallback);
   }
}
