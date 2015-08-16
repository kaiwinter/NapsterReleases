package com.github.kaiwinter.napsterreleases.ui.controller;

import java.util.stream.Collectors;

import com.github.kaiwinter.rhapsody.model.ArtistData;
import com.github.kaiwinter.rhapsody.model.BioData;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

/**
 * Controller for the Artist tab.
 */
public final class ArtistTabController {

	@FXML
	private TextField artistNameTf;

	@FXML
	private TextArea artistBioTa;

	@FXML
	private TextArea artistBlurbsTa;

	@FXML
	private ImageView artistImageIv;

	@FXML
	private ProgressIndicator loadingIndicator;
	@FXML
	private Region loadingIndicatorBackground;

	public void setArtist(ArtistData artistData) {
		artistNameTf.setText(artistData.name);
	}

	public void setBio(BioData bio) {
		artistBioTa.setText(bio.bio);
		String blurbs = bio.blurbs.stream().collect(Collectors.joining(",\n"));
		artistBlurbsTa.setText(blurbs);
	}

	public void clearData() {
		artistNameTf.setText(null);
		artistBioTa.setText(null);
		artistBlurbsTa.setText(null);
		artistImageIv.setImage(null);
	}

	public void setLoading(boolean loading) {
		loadingIndicator.visibleProperty().set(loading);
		loadingIndicatorBackground.visibleProperty().set(loading);
	}

	public void setArtistImage(Image image) {
		artistImageIv.setImage(image);
	}
}
