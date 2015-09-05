package com.github.kaiwinter.napsterreleases.ui.model;

import com.github.kaiwinter.rhapsody.model.AlbumData.Artist;

public final class WatchedArtist {
	public Artist artist;
	public LastRelease lastRelease;

	public WatchedArtist(Artist artist) {
		this.artist = artist;
	}

	public static final class LastRelease {
		public String date;
		public String albumName;
	}
}
