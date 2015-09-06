package com.github.kaiwinter.napsterreleases.ui.controller;

import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;

public final class ArtistWatchlistTabViewModel {

	private BooleanProperty loading = new SimpleBooleanProperty();

	private ListProperty<WatchedArtist> watchedArtists = new SimpleListProperty<>();

	public BooleanProperty loadingProperty() {
		return this.loading;
	}

	public ListProperty<WatchedArtist> watchedArtists() {
		return this.watchedArtists;
	}
}
