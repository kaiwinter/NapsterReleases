package com.github.kaiwinter.napsterreleases.persistence;

import java.util.prefs.Preferences;

import javax.inject.Singleton;

import com.github.kaiwinter.napsterreleases.application.NapsterReleasesMain;

/**
 * Stores user settings in the {@link Preferences}.
 */
@Singleton
public final class UISettings {
   private static final String COLUMN_VISIBLE_ARTIST = "COLUMN_VISIBLE_ARTIST";
   private static final String COLUMN_VISIBLE_ALBUM = "COLUMN_VISIBLE_ALBUM";
   private static final String COLUMN_VISIBLE_RELEASED = "COLUMN_VISIBLE_RELEASED";
   private static final String COLUMN_VISIBLE_TYPE = "COLUMN_VISIBLE_TYPE";
   private static final String COLUMN_VISIBLE_DISCS = "COLUMN_VISIBLE_DISCS";

   private boolean getBoolean(String property, boolean defaultValue) {
      Preferences preferences = Preferences.userNodeForPackage(NapsterReleasesMain.class);
      boolean value = preferences.getBoolean(property, defaultValue);
      return value;
   }

   private void saveBoolean(String property, boolean value) {
      Preferences preferences = Preferences.userNodeForPackage(NapsterReleasesMain.class);
      preferences.putBoolean(property, value);
   }

   public boolean isArtistColumnVisible() {
      return getBoolean(COLUMN_VISIBLE_ARTIST, true);
   }

   public boolean isAlbumColumnVisible() {
      return getBoolean(COLUMN_VISIBLE_ALBUM, true);
   }

   public boolean isReleasedColumnVisible() {
      return getBoolean(COLUMN_VISIBLE_RELEASED, true);
   }

   public boolean isTypeColumnVisible() {
      return getBoolean(COLUMN_VISIBLE_TYPE, true);
   }

   public boolean isDiscColumnVisible() {
      return getBoolean(COLUMN_VISIBLE_DISCS, true);
   }

   public void setArtistColumnVisible(boolean visible) {
      saveBoolean(COLUMN_VISIBLE_ARTIST, visible);
   }

   public void setAlbumColumnVisible(boolean visible) {
      saveBoolean(COLUMN_VISIBLE_ALBUM, visible);
   }

   public void setReleasedColumnVisible(boolean visible) {
      saveBoolean(COLUMN_VISIBLE_RELEASED, visible);
   }

   public void setTypeColumnVisible(boolean visible) {
      saveBoolean(COLUMN_VISIBLE_TYPE, visible);
   }

   public void setDiscColumnVisible(boolean visible) {
      saveBoolean(COLUMN_VISIBLE_DISCS, visible);
   }
}
