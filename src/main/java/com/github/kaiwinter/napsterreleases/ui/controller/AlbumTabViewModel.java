package com.github.kaiwinter.napsterreleases.ui.controller;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.kaiwinter.napsterreleases.util.TimeUtil;
import com.github.kaiwinter.rhapsody.model.AlbumData;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

public final class AlbumTabViewModel {

	private BooleanProperty loading = new SimpleBooleanProperty();

	private StringProperty albumName = new SimpleStringProperty();
	private StringProperty artistName = new SimpleStringProperty();
	private StringProperty discCount = new SimpleStringProperty();
	private StringProperty type = new SimpleStringProperty();
	private StringProperty tags = new SimpleStringProperty();
	private StringProperty releaseDate = new SimpleStringProperty();
	private StringProperty tracks = new SimpleStringProperty();
	private ObjectProperty<Image> image = new SimpleObjectProperty<>();

	public void setAlbum(AlbumData albumData) {
		this.artistName.set(albumData.artist.name);
		this.discCount.set(String.valueOf(albumData.discCount));

		if (albumData.images.size() > 0) {
			String image = albumData.images.get(0).url;
			this.image.set(new Image(image, true));
		}

		this.albumName.set(albumData.name);

		String releaseDate = TimeUtil.timestampToString(albumData.released);
		this.releaseDate.set(releaseDate);

		String tags = albumData.tags.stream().collect(Collectors.joining(", "));
		this.tags.set(tags);

		String tracks = IntStream.range(0, albumData.tracks.size()) //
				.mapToObj(i -> (i + 1) + ". " + albumData.tracks.get(i).name + " - ("
						+ TimeUtil.secondsToString(albumData.tracks.get(i).duration) + ")") //
				.collect(Collectors.joining("\n"));
		this.tracks.set(tracks);

		this.type.set(albumData.type.name);
	}

	public BooleanProperty loadingProperty() {
		return this.loading;
	}

	public StringProperty albumNameProperty() {
		return this.albumName;
	}

	public StringProperty artistNameProperty() {
		return this.artistName;
	}

	public StringProperty discCountProperty() {
		return this.discCount;
	}

	public StringProperty typeProperty() {
		return this.type;
	}

	public StringProperty tagsProperty() {
		return this.tags;
	}

	public StringProperty releaseDateProperty() {
		return this.releaseDate;
	}

	public StringProperty tracksProperty() {
		return this.tracks;
	}

	public ObjectProperty<Image> imageProperty() {
		return this.image;
	}

	public void clear() {
		albumName.set(null);
		artistName.set(null);
		discCount.set(null);
		type.set(null);
		tags.set(null);
		releaseDate.set(null);
		tracks.set(null);
	}
}
