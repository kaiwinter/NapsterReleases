package com.github.kaiwinter.napsterreleases.ui.viewmodel;

import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.rhapsody.api.ArtistImageSize;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

      sharedViewModel.getRhapsodySdkWrapper().loadArtistMeta(artistId, new Callback<ArtistData>() {

         @Override
         public void onResponse(Call<ArtistData> call, Response<ArtistData> response) {
            if (response.isSuccessful()) {
               LOGGER.info("Loaded artist '{}'", response.body().name);
               nameProperty().set(response.body().name);
            } else {
               LOGGER.error("Error loading artist ({})", response.message());
               sharedViewModel.handleError(new Throwable(response.message()), response.code(), () -> showArtist());
            }
         }

         @Override
         public void onFailure(Call<ArtistData> call, Throwable throwable) {
            LOGGER.error("Error loading artist ({})", throwable.getMessage());
            sharedViewModel.handleError(new Throwable(throwable.getMessage()), -1, () -> showArtist());
         }
      });

      sharedViewModel.getRhapsodySdkWrapper().loadArtistBio(artistId, new Callback<BioData>() {

         @Override
         public void onResponse(Call<BioData> call, Response<BioData> response) {
            if (response.isSuccessful()) {
               LOGGER.info("Loaded bio, empty: {}, blurbs #: {}", response.body().bio.isEmpty(),
                  response.body().blurbs.size());
               String blurbs = response.body().blurbs.stream().collect(Collectors.joining(",\n"));
               bioProperty().set(response.body().bio);
               blubsProperty().set(blurbs);
               loadingProperty().set(false);
            } else {
               loadingProperty().set(false);
               LOGGER.error("Error loading bio ({})", response.message());
               sharedViewModel.handleError(new Throwable(response.message()), response.code(), () -> showArtist());
            }
         }

         @Override
         public void onFailure(Call<BioData> call, Throwable throwable) {
            loadingProperty().set(false);
            LOGGER.error("Error loading bio ({})", throwable.getMessage());
            sharedViewModel.handleError(throwable, -1, () -> showArtist());
         }
      });
   }
}
