package com.github.kaiwinter.napsterreleases.ui.model;

import com.github.kaiwinter.rhapsody.model.AlbumData.Artist;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class WatchedArtist {
	private final Artist artist;
	private final ObjectProperty<LastRelease> lastRelease = new SimpleObjectProperty<>(new LastRelease());

	public WatchedArtist(Artist artist) {
		this.artist = artist;
	}

	public Artist getArtist() {
		return artist;
	}

	public ObjectProperty<LastRelease> lastReleaseProperty() {
		return this.lastRelease;
	}

	public LastRelease getLastRelease() {
		return this.lastReleaseProperty().get();
	}

	public void setLastRelease(LastRelease lastRelease) {
		this.lastReleaseProperty().set(lastRelease);
	}

	public static final class LastRelease {
		private StringProperty date = new SimpleStringProperty();
		private StringProperty albumName = new SimpleStringProperty();

		public StringProperty dateProperty() {
			return this.date;
		}

		public String getDate() {
			return this.dateProperty().get();
		}

		public void setDate(String date) {
			this.dateProperty().set(date);
		}

		public StringProperty albumNameProperty() {
			return this.albumName;
		}

		public String getAlbumName() {
			return this.albumNameProperty().get();
		}

		public void setAlbumName(String albumName) {
			this.albumNameProperty().set(albumName);
		}
	}
}
