package com.github.kaiwinter.napsterreleases.ui.model;

import com.github.kaiwinter.rhapsody.model.AlbumData.Artist;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

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
		private final StringProperty date = new SimpleStringProperty();
		private final StringProperty albumName = new SimpleStringProperty();
		private final BooleanProperty updated = new SimpleBooleanProperty(false);

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

		public BooleanProperty updatedProperty() {
			return this.updated;
		}

		public boolean isUpdated() {
			return this.updatedProperty().get();
		}

		public void setUpdated(boolean updated) {
			this.updatedProperty().set(updated);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((albumName.get() == null) ? 0 : albumName.get().hashCode());
			result = prime * result + ((date.get() == null) ? 0 : date.get().hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LastRelease other = (LastRelease) obj;
			if (albumName.get() == null) {
				if (other.albumName.get() != null)
					return false;
			} else if (!albumName.get().equals(other.albumName.get()))
				return false;
			if (date.get() == null) {
				if (other.date.get() != null)
					return false;
			} else if (!date.get().equals(other.date.get()))
				return false;
			return true;
		}

	}
}
