package com.github.kaiwinter.napsterreleases.ui.model;

import com.github.kaiwinter.rhapsody.model.AlbumData.Artist;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Stores the last released album for an Artist.
 */
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

	/**
	 * The last released album and release date of an Artist.
	 */
	public static final class LastRelease {
		private final StringProperty id = new SimpleStringProperty(); // The Rhapsody ID
		private final StringProperty date = new SimpleStringProperty();
		private final StringProperty albumName = new SimpleStringProperty();
		private final ObjectProperty<Paint> textColor = new SimpleObjectProperty<Paint>(Color.BLACK);

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

		public ObjectProperty<Paint> textColorProperty() {
			return this.textColor;
		}

		public Paint getTextColor() {
			return this.textColorProperty().get();
		}

		public void setTextColor(Paint updated) {
			this.textColorProperty().set(updated);
		}

		public StringProperty idProperty() {
			return this.id;
		}

		public String getId() {
			return this.idProperty().get();
		}

		public void setId(String id) {
			this.idProperty().set(id);
		}
	}
}
