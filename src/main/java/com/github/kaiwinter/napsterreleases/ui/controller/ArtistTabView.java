package com.github.kaiwinter.napsterreleases.ui.controller;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

/**
 * Artist tab view.
 */
public final class ArtistTabView implements FxmlView<ArtistTabViewModel> {

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

	@InjectViewModel
	private ArtistTabViewModel viewModel;

	@FXML
	public void initialize() {
		viewModel.nameProperty().bindBidirectional(artistNameTf.textProperty());
		viewModel.bioProperty().bindBidirectional(artistBioTa.textProperty());
		viewModel.blubsProperty().bindBidirectional(artistBlurbsTa.textProperty());
		viewModel.imageProperty().bindBidirectional(artistImageIv.imageProperty());

		viewModel.loadingProperty().bindBidirectional(loadingIndicator.visibleProperty());
		viewModel.loadingProperty().bindBidirectional(loadingIndicatorBackground.visibleProperty());
	}
}
