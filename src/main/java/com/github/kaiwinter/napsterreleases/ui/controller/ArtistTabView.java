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

	private ArtistTabViewModel viewModel;

	@FXML
	public void initialize() {
		this.viewModel = new ArtistTabViewModel();
		this.viewModel.nameProperty().bindBidirectional(artistNameTf.textProperty());
		this.viewModel.bioProperty().bindBidirectional(artistBioTa.textProperty());
		this.viewModel.blubsProperty().bindBidirectional(artistBlurbsTa.textProperty());
		this.viewModel.imageProperty().bindBidirectional(artistImageIv.imageProperty());

		this.viewModel.loadingProperty().bindBidirectional(loadingIndicator.visibleProperty());
		this.viewModel.loadingProperty().bindBidirectional(loadingIndicatorBackground.visibleProperty());
	}

	public ArtistTabViewModel getViewModel() {
		return viewModel;
	}
}
