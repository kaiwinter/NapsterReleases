package com.github.kaiwinter.napsterreleases.ui.controller;

import com.github.kaiwinter.jfx.tablecolumn.filter.FilterSupport;
import com.github.kaiwinter.rhapsody.model.AlbumData;
import com.github.kaiwinter.rhapsody.model.GenreData;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TreeItem;

public final class NewReleasesTabViewModel {
	private BooleanProperty loading = new SimpleBooleanProperty();

	private ObjectProperty<TreeItem<GenreData>> genres = new SimpleObjectProperty<>();
	private ListProperty<AlbumData> releases = new SimpleListProperty<>();
	private StringProperty genreDescription = new SimpleStringProperty();

	private ObjectProperty<TreeItem<GenreData>> selectedGenre = new SimpleObjectProperty<>();
	private ObjectProperty<AlbumData> selectedAlbum = new SimpleObjectProperty<>();

	private BooleanProperty artistColumVisible = new SimpleBooleanProperty();
	private BooleanProperty albumColumVisible = new SimpleBooleanProperty();
	private BooleanProperty releasedColumVisible = new SimpleBooleanProperty();
	private BooleanProperty typeColumVisible = new SimpleBooleanProperty();
	private BooleanProperty discsColumVisible = new SimpleBooleanProperty();

	public BooleanProperty loadingProperty() {
		return this.loading;
	}

	public ObjectProperty<TreeItem<GenreData>> genresProperty() {
		return this.genres;
	}

	public ListProperty<AlbumData> releasesProperty() {
		return this.releases;
	}

	public StringProperty genreDescriptionProperty() {
		return this.genreDescription;
	}

	public void clearData() {
		genres.set(null);
		FilterSupport.getUnwrappedList(releases.get()).clear();
		genreDescription.set(null);
	}

	public ObjectProperty<TreeItem<GenreData>> selectedGenreProperty() {
		return this.selectedGenre;
	}

	public ObjectProperty<AlbumData> selectedAlbumProperty() {
		return this.selectedAlbum;
	}

	public BooleanProperty artistColumVisibleProperty() {
		return this.artistColumVisible;
	}

	public BooleanProperty albumColumVisibleProperty() {
		return this.albumColumVisible;
	}

	public BooleanProperty releasedColumVisibleProperty() {
		return this.releasedColumVisible;
	}

	public BooleanProperty typeColumVisibleProperty() {
		return this.typeColumVisible;
	}

	public BooleanProperty discsColumVisibleProperty() {
		return this.discsColumVisible;
	}
}
