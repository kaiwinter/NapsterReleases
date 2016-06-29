package com.github.kaiwinter.napsterreleases.ui;

import com.github.kaiwinter.napsterreleases.util.TimeUtil;
import com.github.kaiwinter.rhapsody.model.AlbumData;

import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

/**
 * Collects CellValueFactories for the {@link AlbumData}.
 */
public final class AlbumDataCellValueFactories {

   public static final class AlbumNameValueFactory
      implements Callback<TableColumn.CellDataFeatures<AlbumData, String>, ObservableValue<String>> {
      @Override
      public ObservableValue<String> call(CellDataFeatures<AlbumData, String> value) {
         return new ObservableValueBase<String>() {
            @Override
            public String getValue() {
               return value.getValue().name;
            }
         };
      }
   }

   public static final class ArtistNameValueFactory
      implements Callback<TableColumn.CellDataFeatures<AlbumData, String>, ObservableValue<String>> {
      @Override
      public ObservableValue<String> call(CellDataFeatures<AlbumData, String> value) {
         return new ObservableValueBase<String>() {
            @Override
            public String getValue() {
               return value.getValue().artist.name;
            }
         };
      }
   }

   public static final class DiscCountValueFactory
      implements Callback<TableColumn.CellDataFeatures<AlbumData, String>, ObservableValue<String>> {
      @Override
      public ObservableValue<String> call(CellDataFeatures<AlbumData, String> value) {
         return new ObservableValueBase<String>() {
            @Override
            public String getValue() {
               return String.valueOf(value.getValue().discCount);
            }
         };
      }
   }

   public static final class ReleaseDateValueFactory
      implements Callback<TableColumn.CellDataFeatures<AlbumData, String>, ObservableValue<String>> {
      @Override
      public ObservableValue<String> call(CellDataFeatures<AlbumData, String> value) {
         return new ObservableValueBase<String>() {
            @Override
            public String getValue() {
               return TimeUtil.timestampToString(value.getValue().released);
            }
         };
      }
   }

   public static final class TypeValueFactory
      implements Callback<TableColumn.CellDataFeatures<AlbumData, String>, ObservableValue<String>> {
      @Override
      public ObservableValue<String> call(CellDataFeatures<AlbumData, String> value) {
         return new ObservableValueBase<String>() {
            @Override
            public String getValue() {
               return value.getValue().type.name;
            }
         };
      }
   }

}
