package com.github.kaiwinter.napsterreleases.ui.controller;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.napsterreleases.util.TimeUtil;
import com.github.kaiwinter.rhapsody.api.RhapsodySdkWrapper;
import com.github.kaiwinter.rhapsody.model.AlbumData;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.image.Image;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public final class AlbumTabViewModel {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlbumTabViewModel.class.getSimpleName());

	private final BooleanProperty loading = new SimpleBooleanProperty();

	private final StringProperty albumName = new SimpleStringProperty();
	private final StringProperty artistName = new SimpleStringProperty();
	private final StringProperty discCount = new SimpleStringProperty();
	private final StringProperty type = new SimpleStringProperty();
	private final StringProperty tags = new SimpleStringProperty();
	private final StringProperty releaseDate = new SimpleStringProperty();
	private final StringProperty tracks = new SimpleStringProperty();
	private final ObjectProperty<Image> image = new SimpleObjectProperty<>();
	private final ObjectProperty<AlbumData> selectedAlbum = new SimpleObjectProperty<AlbumData>();

	private final RhapsodySdkWrapper rhapsodySdkWrapper;

	private MainViewModel viewModel;

	public AlbumTabViewModel(RhapsodySdkWrapper rhapsodySdkWrapper) {
		this.rhapsodySdkWrapper = rhapsodySdkWrapper;
		selectedAlbum.addListener((ChangeListener<AlbumData>) (observable, oldValue, newValue) -> clear());
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

	public ObjectProperty<AlbumData> selectedAlbumProperty() {
		return this.selectedAlbum;
	}

	private void clear() {
		albumName.set(null);
		artistName.set(null);
		discCount.set(null);
		type.set(null);
		tags.set(null);
		releaseDate.set(null);
		tracks.set(null);
		image.set(null);
	}

	public void showAlbum() {
		AlbumData albumData = selectedAlbum.get();
		if (albumData == null) {
			return;
		}
		String albumId = albumData.id;
		loadingProperty().set(true);
		rhapsodySdkWrapper.loadAlbum(albumId, new Callback<AlbumData>() {

			@Override
			public void success(AlbumData albumData, Response response) {
				LOGGER.info("Loaded album '{}'", albumData.name);
				setAlbum(albumData);
				loadingProperty().set(false);
			}

			@Override
			public void failure(RetrofitError error) {
				loadingProperty().set(false);
				LOGGER.error("Error loading album ({})", error.getMessage());
				viewModel.handleError(error, () -> showAlbum());
			}
		});
	}

	private void setAlbum(AlbumData albumData) {
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

	public void setMainViewModel(MainViewModel viewModel) {
		this.viewModel = viewModel;
	}
}
