package com.github.kaiwinter.napsterreleases.ui.controller;

import com.github.kaiwinter.jfx.tablecolumn.filter.FilterSupport;
import com.github.kaiwinter.napsterreleases.UserSettings;
import com.github.kaiwinter.napsterreleases.util.TimeUtil;
import com.github.kaiwinter.rhapsody.model.AlbumData;
import com.github.kaiwinter.rhapsody.model.GenreData;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * Controller for the New Releases tab.
 */
public final class NewReleasesTabView {

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
	private NewReleasesTabViewModel viewModel;

	@FXML
	private void initialize() {
		this.viewModel = new NewReleasesTabViewModel();
		this.viewModel.genres().bindBidirectional(genreList.rootProperty());
		this.viewModel.releases().bindBidirectional(releasesTv.itemsProperty());
		this.viewModel.genreDescription().bindBidirectional(textArea.textProperty());

		this.viewModel.selectedGenre().bind(genreList.getSelectionModel().selectedItemProperty());
		this.viewModel.selectedAlbum().bind(releasesTv.getSelectionModel().selectedItemProperty());

		this.viewModel.loadingProperty().bindBidirectional(loadingIndicator.visibleProperty());
		this.viewModel.loadingProperty().bindBidirectional(loadingIndicatorBackground.visibleProperty());

		userSettings = new UserSettings();
		prepareUi();

		SortedList<AlbumData> sorted = FXCollections.<AlbumData> observableArrayList().filtered(null).sorted();
		releasesTv.setItems(sorted);
		sorted.comparatorProperty().bind(releasesTv.comparatorProperty());
		this.viewModel.releases().bindBidirectional(releasesTv.itemsProperty());

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
		mainController.loadGenres();
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
			FilterSupport.getItems(releasesTv).clear();
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

			ContextMenu artistColumnContextMenu = new ContextMenu();
			MenuItem addToWatchlistMenuItem = new MenuItem("Add Artist to Watchlist");
			addToWatchlistMenuItem.setOnAction((e) -> {
				AlbumData selectedItem = releasesTv.getSelectionModel().getSelectedItem();
				mainController.addArtistToWatchlist(selectedItem.artist);
			});
			artistColumnContextMenu.getItems().add(addToWatchlistMenuItem);
			row.contextMenuProperty().bind(
					Bindings.when(Bindings.isNotNull(row.itemProperty())).then(artistColumnContextMenu).otherwise((ContextMenu) null));

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

		artistTc.setCellFactory(param -> {
			TableCell<AlbumData, String> tableCell = new TextFieldTableCell<>();

			tableCell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
				if (event.getClickCount() == 2) {
					mainController.switchToArtistTab();
				}
			});

			return tableCell;
		});

		albumTc.setCellFactory(param -> {
			TableCell<AlbumData, String> tableCell = new TextFieldTableCell<>();

			tableCell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
				if (event.getClickCount() == 2) {
					mainController.switchToAlbumTab();
				}
			});

			return tableCell;
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

	public NewReleasesTabViewModel getViewModel() {
		return viewModel;
	}
}
