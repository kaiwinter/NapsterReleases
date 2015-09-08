package com.github.kaiwinter.napsterreleases.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

/**
 * Artist tab view.
 */
public final class ArtistTabView {

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

	public void setViewModel(ArtistTabViewModel viewModel) {
		viewModel.nameProperty().bindBidirectional(artistNameTf.textProperty());
		viewModel.bioProperty().bindBidirectional(artistBioTa.textProperty());
		viewModel.blubsProperty().bindBidirectional(artistBlurbsTa.textProperty());
		viewModel.imageProperty().bindBidirectional(artistImageIv.imageProperty());

		viewModel.loadingProperty().bindBidirectional(loadingIndicator.visibleProperty());
		viewModel.loadingProperty().bindBidirectional(loadingIndicatorBackground.visibleProperty());
	}
}
