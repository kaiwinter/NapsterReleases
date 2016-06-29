package com.github.kaiwinter.napsterreleases.ui.viewmodel;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.rhapsody.model.AlbumData;
import com.github.kaiwinter.rhapsody.model.ArtistData;
import com.github.kaiwinter.rhapsody.model.member.ChartsAlbum;
import com.github.kaiwinter.rhapsody.model.member.ChartsArtist;
import com.github.kaiwinter.rhapsody.service.member.ChartService.RangeEnum;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

@Singleton
public final class ChartsTabViewModel implements ViewModel {
   private static final Logger LOGGER = LoggerFactory.getLogger(ChartsTabViewModel.class.getSimpleName());

   private final BooleanProperty loading = new SimpleBooleanProperty();

   private final StringProperty artistsText = new SimpleStringProperty();
   private final StringProperty albumText = new SimpleStringProperty();

   private final BooleanProperty tabSelected = new SimpleBooleanProperty();

   @Inject
   private SharedViewModel sharedViewModel;

   public BooleanProperty loadingProperty() {
      return this.loading;
   }

   public void loadCharts() {

      Callback<List<ChartsArtist>> artistsCallback = new Callback<List<ChartsArtist>>() {

         @Override
         public void success(List<ChartsArtist> artists, Response response) {
            String string = "";
            for (ChartsArtist chartsArtist : artists) {
               ArtistData artistMeta = sharedViewModel.getRhapsodySdkWrapper().getArtistMeta(chartsArtist.id);
               if (!string.isEmpty()) {
                  string += "\n";
               }
               string += artistMeta.name + " (" + chartsArtist.playCount + " times)";

               artistsTextProperty().set(string);
            }
         }

         @Override
         public void failure(RetrofitError error) {
            LOGGER.error(error.getMessage(), error);
         }
      };
      sharedViewModel.getRhapsodySdkWrapper().loadTopPlayedArtists(null, RangeEnum.life, artistsCallback);
      //
      Callback<List<ChartsAlbum>> albumCallback = new Callback<List<ChartsAlbum>>() {

         @Override
         public void success(List<ChartsAlbum> albums, Response response) {
            String string = "";
            for (ChartsAlbum chartsAlbum : albums) {
               try {
                  AlbumData albumData = sharedViewModel.getRhapsodySdkWrapper().getAlbum(chartsAlbum.id);
                  if (!string.isEmpty()) {
                     string += "\n";
                  }
                  string += albumData.name + " - " + albumData.artist.name + " (" + chartsAlbum.playCount + " times)";

                  albumTextProperty().set(string);
               } catch (RetrofitError error) {
                  LOGGER.warn(error.getMessage(), error);
               }
            }

            loading.set(false);
         }

         @Override
         public void failure(RetrofitError error) {
            LOGGER.error(error.getMessage(), error);
            sharedViewModel.handleError(error, () -> loadCharts());
            loading.set(false);
         }
      };
      sharedViewModel.getRhapsodySdkWrapper().loadTopPlayedAlbums(null, RangeEnum.life, albumCallback);
   }

   public StringProperty artistsTextProperty() {
      return this.artistsText;
   }

   public StringProperty albumTextProperty() {
      return this.albumText;
   }

   public BooleanProperty tabSelectedProperty() {
      return this.tabSelected;
   }
}
