package com.github.kaiwinter.napsterreleases.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

/**
 * Album tab view.
 */
public final class AlbumTabView {

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

	private AlbumTabViewModel viewModel;

	@FXML
	public void initialize() {
		this.viewModel = new AlbumTabViewModel();
		this.viewModel.albumNameProperty().bindBidirectional(albumNameTf.textProperty());
		this.viewModel.artistNameProperty().bindBidirectional(albumArtistTf.textProperty());
		this.viewModel.discCountProperty().bindBidirectional(albumDiscCountTf.textProperty());
		this.viewModel.typeProperty().bindBidirectional(albumTypeTf.textProperty());
		this.viewModel.tagsProperty().bindBidirectional(albumTagsTf.textProperty());
		this.viewModel.releaseDateProperty().bindBidirectional(albumReleaseDateTf.textProperty());
		this.viewModel.tracksProperty().bindBidirectional(albumTracksTa.textProperty());
		this.viewModel.imageProperty().bindBidirectional(albumImageIv.imageProperty());

		this.viewModel.loadingProperty().bindBidirectional(loadingIndicator.visibleProperty());
		this.viewModel.loadingProperty().bindBidirectional(loadingIndicatorBackground.visibleProperty());
	}

	public AlbumTabViewModel getViewModel() {
		return viewModel;
	}
}
