package com.github.kaiwinter.napsterreleases;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

import com.github.kaiwinter.napsterreleases.ui.NapsterReleasesMain;
import com.github.kaiwinter.rhapsody.model.AlbumData.Artist;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Stores user settings in the {@link Preferences}.
 */
public final class UserSettings {

	private static final String COLUMN_VISIBLE_ARTIST = "COLUMN_VISIBLE_ARTIST";
	private static final String COLUMN_VISIBLE_ALBUM = "COLUMN_VISIBLE_ALBUM";
	private static final String COLUMN_VISIBLE_RELEASED = "COLUMN_VISIBLE_RELEASED";
	private static final String COLUMN_VISIBLE_TYPE = "COLUMN_VISIBLE_TYPE";
	private static final String COLUMN_VISIBLE_DISCS = "COLUMN_VISIBLE_DISCS";
	private static final String WATCHED_ARTISTS = "WATCHED_ARTISTS";

	private boolean getBoolean(String property, boolean defaultValue) {
		Preferences preferences = Preferences.userNodeForPackage(NapsterReleasesMain.class);
		boolean value = preferences.getBoolean(property, defaultValue);
		return value;
	}

	private void saveBoolean(String property, boolean value) {
		Preferences preferences = Preferences.userNodeForPackage(NapsterReleasesMain.class);
		preferences.putBoolean(property, value);
	}

	private String getString(String property, String defaultValue) {
		Preferences preferences = Preferences.userNodeForPackage(NapsterReleasesMain.class);
		String value = preferences.get(property, defaultValue);
		return value;
	}

	private void saveString(String property, String value) {
		Preferences preferences = Preferences.userNodeForPackage(NapsterReleasesMain.class);

		if (value.isEmpty()) {
			preferences.remove(property);
		} else {
			preferences.put(property, value);
		}
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

	public Set<Artist> loadWatchedArtists() {
		String string = getString(WATCHED_ARTISTS, null);
		if (string == null) {
			return new HashSet<>();
		}
		Type collectionType = new TypeToken<Set<Artist>>() {
		}.getType();

		Set<Artist> fromJson = new Gson().fromJson(string, collectionType);
		return fromJson;
	}

	public void saveWatchedArtists(Set<Artist> watchedArtists) {
		String json = new Gson().toJson(watchedArtists);
		saveString(WATCHED_ARTISTS, json);
	}
}
