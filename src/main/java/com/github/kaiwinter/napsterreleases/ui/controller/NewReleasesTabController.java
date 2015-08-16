package com.github.kaiwinter.napsterreleases.ui.controller;

import java.util.ArrayList;
import java.util.Collection;

import com.github.kaiwinter.jfx.tablecolumn.filter.FilterSupport;
import com.github.kaiwinter.napsterreleases.UserSettings;
import com.github.kaiwinter.napsterreleases.util.TimeUtil;
import com.github.kaiwinter.rhapsody.model.AlbumData;
import com.github.kaiwinter.rhapsody.model.GenreData;

import javafx.application.Platform;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Region;

/**
 * Controller for the New Releases tab.
 */
public final class NewReleasesTabController {

	@FXML
	private TreeView<GenreData> genreList;

	@FXML
	private TextArea textArea;

	@FXML
	private TableView<AlbumData> releasesTv;

	@FXML
	private TableColumn<AlbumData, String> artistTc;
	@FXML
	private TableColumn<AlbumData, String> albumTc;
	@FXML
	private TableColumn<AlbumData, String> releasedTc;
	@FXML
	private TableColumn<AlbumData, String> typeTc;
	@FXML
	private TableColumn<AlbumData, String> discsTc;

	@FXML
	private ProgressIndicator loadingIndicator;
	@FXML
	private Region loadingIndicatorBackground;

	private MainController mainController;

	private UserSettings userSettings;

	@FXML
	private void initialize() {
		userSettings = new UserSettings();
		prepareUi();

		// Initial sort by release data
		releasesTv.getSortOrder().add(releasedTc);

		FilterSupport.addFilter(artistTc);
		FilterSupport.addFilter(albumTc);
		FilterSupport.addFilter(releasedTc);
		FilterSupport.addFilter(typeTc);

		loadColumnVisibility();
		addColumnVisibilityListeners();
	}

	@FXML
	private void loadGenres() {
		genreList.setRoot(null);
		mainController.showGenres();
	}

	@FXML
	private void logout() {
		mainController.logout();
	}

	private void prepareUi() {
		genreList.setShowRoot(false);
		genreList.setCellFactory(param -> {
			TreeCell<GenreData> listCell = new TreeCell<GenreData>() {
				@Override
				protected void updateItem(GenreData item, boolean empty) {
					super.updateItem(item, empty);
					if (empty) {
						setText(null);
					} else {
						setText(item.name);
					}
				};
			};
			return listCell;
		});

		genreList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			FilterSupport.clearItems(releasesTv);
			mainController.clearDetailTabs();

			if (newValue == null) {
				textArea.clear();
			} else {
				textArea.setText(newValue.getValue().description);
				mainController.showNewReleases(newValue.getValue());
			}
		});

		releasesTv.getSelectionModel().selectedItemProperty()
				.addListener((observable, oldValue, newValue) -> mainController.clearDetailTabs());
		releasesTv.setRowFactory(tv -> {
			TableRow<AlbumData> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					mainController.switchToAlbumTab();
				}
			});
			return row;
		});

		artistTc.setCellValueFactory(value -> new ObservableValueBase<String>() {
			@Override
			public String getValue() {
				return value.getValue().artist.name;
			}
		});

		albumTc.setCellValueFactory(value -> new ObservableValueBase<String>() {
			@Override
			public String getValue() {
				return value.getValue().name;
			}
		});

		releasedTc.setCellValueFactory(value -> new ObservableValueBase<String>() {
			@Override
			public String getValue() {
				return TimeUtil.timestampToString(value.getValue().released);
			}
		});

		typeTc.setCellValueFactory(value -> new ObservableValueBase<String>() {
			@Override
			public String getValue() {
				return value.getValue().type.name;
			}
		});

		discsTc.setCellValueFactory(value -> new ObservableValueBase<String>() {
			@Override
			public String getValue() {
				return String.valueOf(value.getValue().discCount);
			}
		});
	}

	private void loadColumnVisibility() {
		// artistTc.setVisible(userSettings.getArtistColumnVisible());
		albumTc.setVisible(userSettings.isAlbumColumnVisible());
		releasedTc.setVisible(userSettings.isReleasedColumnVisible());
		typeTc.setVisible(userSettings.isTypeColumnVisible());
		discsTc.setVisible(userSettings.isDiscColumnVisible());
	}

	private void addColumnVisibilityListeners() {
		artistTc.visibleProperty().addListener((observable, oldValue, newValue) -> userSettings.setArtistColumnVisible(newValue));
		albumTc.visibleProperty().addListener((observable, oldValue, newValue) -> userSettings.setAlbumColumnVisible(newValue));
		releasedTc.visibleProperty().addListener((observable, oldValue, newValue) -> userSettings.setReleasedColumnVisible(newValue));
		typeTc.visibleProperty().addListener((observable, oldValue, newValue) -> userSettings.setTypeColumnVisible(newValue));
		discsTc.visibleProperty().addListener((observable, oldValue, newValue) -> userSettings.setDiscColumnVisible(newValue));
	}

	public void setMainController(MainController mainController) {
		this.mainController = mainController;
	}

	public void setGenres(Collection<GenreData> genres) {
		Platform.runLater(() -> {
			TreeItem<GenreData> root = new TreeItem<>();
			for (GenreData genreData : genres) {
				TreeItem<GenreData> treeViewItem = new TreeItem<>(genreData);
				root.getChildren().add(treeViewItem);
				if (genreData.subgenres != null) {
					for (GenreData subgenre : genreData.subgenres) {
						treeViewItem.getChildren().add(new TreeItem<>(subgenre));
					}
				}
			}

			genreList.setRoot(root);
		});
	}

	public void setNewReleases(Collection<AlbumData> albums) {
		ArrayList<TableColumn<AlbumData, ?>> savesSortOrder = new ArrayList<>(releasesTv.getSortOrder());
		releasesTv.setItems(FXCollections.observableArrayList(albums));
		releasesTv.getSortOrder().addAll(savesSortOrder);

		// Refresh sort
		releasesTv.sort();
	}

	public void clearData() {
		genreList.setRoot(null);
		releasesTv.getItems().clear();
	}

	public AlbumData getSelectedAlbum() {
		AlbumData selectedAlbum = releasesTv.getSelectionModel().getSelectedItem();
		return selectedAlbum;
	}

	public void setLoading(boolean loading) {
		loadingIndicator.visibleProperty().set(loading);
		loadingIndicatorBackground.visibleProperty().set(loading);
	}
}
