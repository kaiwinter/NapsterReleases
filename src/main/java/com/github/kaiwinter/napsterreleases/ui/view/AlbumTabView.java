package com.github.kaiwinter.napsterreleases.ui.view;

import com.github.kaiwinter.napsterreleases.ui.viewmodel.AlbumTabViewModel;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

/**
 * Album tab view.
 */
public final class AlbumTabView implements FxmlView<AlbumTabViewModel> {

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

	@InjectViewModel
	private AlbumTabViewModel viewModel;

	@FXML
	public void initialize() {
		viewModel.albumNameProperty().bindBidirectional(albumNameTf.textProperty());
		viewModel.artistNameProperty().bindBidirectional(albumArtistTf.textProperty());
		viewModel.discCountProperty().bindBidirectional(albumDiscCountTf.textProperty());
		viewModel.typeProperty().bindBidirectional(albumTypeTf.textProperty());
		viewModel.tagsProperty().bindBidirectional(albumTagsTf.textProperty());
		viewModel.releaseDateProperty().bindBidirectional(albumReleaseDateTf.textProperty());
		viewModel.tracksProperty().bindBidirectional(albumTracksTa.textProperty());
		viewModel.imageProperty().bindBidirectional(albumImageIv.imageProperty());

		viewModel.loadingProperty().bindBidirectional(loadingIndicator.visibleProperty());
		viewModel.loadingProperty().bindBidirectional(loadingIndicatorBackground.visibleProperty());
	}
}
