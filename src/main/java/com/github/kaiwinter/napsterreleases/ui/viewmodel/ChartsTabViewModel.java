package com.github.kaiwinter.napsterreleases.ui.viewmodel;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.rhapsody.api.RhapsodyCallback;
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

      RhapsodyCallback<List<ChartsArtist>> artistsCallback = new RhapsodyCallback<List<ChartsArtist>>() {

         @Override
         public void onSuccess(List<ChartsArtist> data) {
            String string = "";
            for (ChartsArtist chartsArtist : data) {
               ArtistData artistMeta = sharedViewModel.getRhapsodySdkWrapper().getArtistMeta(chartsArtist.id);
               if (!string.isEmpty()) {
                  string += "\n";
               }
               string += artistMeta.name + " (" + chartsArtist.playCount + " times)";

               artistsTextProperty().set(string);
            }
         }

         @Override
         public void onFailure(int code, String message) {
            LOGGER.error("Error loading listening charts ({} {})", code, message);
         }
      };
      sharedViewModel.getRhapsodySdkWrapper().loadTopPlayedArtists(null, RangeEnum.life, artistsCallback);

      RhapsodyCallback<List<ChartsAlbum>> albumCallback = new RhapsodyCallback<List<ChartsAlbum>>() {

         @Override
         public void onSuccess(List<ChartsAlbum> data) {
            String string = "";
            for (ChartsAlbum chartsAlbum : data) {
               AlbumData albumData = sharedViewModel.getRhapsodySdkWrapper().getAlbum(chartsAlbum.id);
               if (!string.isEmpty()) {
                  string += "\n";
               }
               if (albumData == null) {
                  string += "<" + chartsAlbum.id + "> - ? (" + chartsAlbum.playCount + " times)";
               } else {
                  string += albumData.name + " - " + albumData.artist.name + " (" + chartsAlbum.playCount + " times)";
               }

               albumTextProperty().set(string);
            }

            loading.set(false);
         }

         @Override
         public void onFailure(int code, String message) {
            LOGGER.error("Error loading listening charts ({} {})", code, message);
            sharedViewModel.handleError(message, code, () -> loadCharts());
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
