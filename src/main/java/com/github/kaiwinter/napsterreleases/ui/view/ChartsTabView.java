package com.github.kaiwinter.napsterreleases.ui.view;

import com.github.kaiwinter.napsterreleases.ui.viewmodel.ChartsTabViewModel;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/**
 * Controller for the charts tab.
 */
public final class ChartsTabView implements FxmlView<ChartsTabViewModel> {

	@FXML
	private TextArea artistsTa;

	@FXML
	private TextArea albumsTa;

	@FXML
	private ProgressIndicator loadingIndicator;
	@FXML
	private Region loadingIndicatorBackground;

	@InjectViewModel
	private ChartsTabViewModel viewModel;

	@FXML
	public void initialize() {
		viewModel.loadingProperty().bindBidirectional(loadingIndicator.visibleProperty());
		viewModel.loadingProperty().bindBidirectional(loadingIndicatorBackground.visibleProperty());

		viewModel.artistsTextProperty().bindBidirectional(artistsTa.textProperty());
		viewModel.albumTextProperty().bindBidirectional(albumsTa.textProperty());

		viewModel.tabSelectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
			// Automatically load if tab is selected and no data was loaded previously
			if (newValue && artistsTa.getText().isEmpty()) {
				viewModel.loadCharts();
			}
		});
	}
}
