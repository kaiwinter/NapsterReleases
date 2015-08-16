package com.github.kaiwinter.napsterreleases.ui.controller;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.kaiwinter.napsterreleases.util.TimeUtil;
import com.github.kaiwinter.rhapsody.model.AlbumData;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

/**
 * Controller for the Album tab
 */
public final class AlbumTabController {

	@FXML
	private TextField albumNameTf;

	@FXML
	private TextField albumDiscCountTf;

	@FXML
	private TextField albumArtistTf;

	@FXML
	private TextField albumTypeTf;

	@FXML
	private TextField albumTagsTf;

	@FXML
	private TextArea albumTracksTa;

	@FXML
	private TextField albumReleaseDateTf;

	@FXML
	private ImageView albumImageIv;

	@FXML
	private ProgressIndicator loadingIndicator;
	@FXML
	private Region loadingIndicatorBackground;

	public void setAlbum(AlbumData albumData) {
		albumArtistTf.setText(albumData.artist.name);
		albumDiscCountTf.setText(String.valueOf(albumData.discCount));

		if (albumData.images.size() > 0) {
			String image = albumData.images.get(0).url;
			albumImageIv.setImage(new Image(image, true));
		}

		albumNameTf.setText(albumData.name);

		String releaseDate = TimeUtil.timestampToString(albumData.released);
		albumReleaseDateTf.setText(releaseDate);

		String tags = albumData.tags.stream().collect(Collectors.joining(", "));
		albumTagsTf.setText(tags);

		String tracks = IntStream.range(0, albumData.tracks.size()) //
				.mapToObj(i -> (i + 1) + ". " + albumData.tracks.get(i).name + " - ("
						+ TimeUtil.secondsToString(albumData.tracks.get(i).duration) + ")") //
				.collect(Collectors.joining("\n"));
		albumTracksTa.setText(tracks);

		albumTypeTf.setText(albumData.type.name);
	}

	public void clearData() {
		albumNameTf.setText(null);
		albumDiscCountTf.setText(null);
		albumArtistTf.setText(null);
		albumTypeTf.setText(null);
		albumTagsTf.setText(null);
		albumTracksTa.setText(null);
		albumReleaseDateTf.setText(null);
		albumImageIv.setImage(null);
	}

	public void setLoading(boolean loading) {
		loadingIndicator.visibleProperty().set(loading);
		loadingIndicatorBackground.visibleProperty().set(loading);
	}
}
