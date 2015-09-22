package com.github.kaiwinter.napsterreleases.ui;

import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Callback;

/**
 * Collects CellFactories and CellValueFactories for the {@link WatchedArtist}.
 */
public final class WatchedArtistCellValueFactory {

	public static final class NameValueFactory
			implements Callback<TableColumn.CellDataFeatures<WatchedArtist, String>, ObservableValue<String>> {
		@Override
		public ObservableValue<String> call(CellDataFeatures<WatchedArtist, String> value) {
			return new ObservableValueBase<String>() {
				@Override
				public String getValue() {
					return value.getValue().getArtist().name;
				}
			};
		}
	}

	public static final class WatchedArtistValueFactory
			implements Callback<TableColumn.CellDataFeatures<WatchedArtist, WatchedArtist>, ObservableValue<WatchedArtist>> {
		@Override
		public ObservableValue<WatchedArtist> call(CellDataFeatures<WatchedArtist, WatchedArtist> value) {
			return new ObservableValueBase<WatchedArtist>() {
				@Override
				public WatchedArtist getValue() {
					return value.getValue();
				}
			};
		}
	}

	public static final class LastReleaseCellFactory
			implements Callback<TableColumn<WatchedArtist, WatchedArtist>, TableCell<WatchedArtist, WatchedArtist>> {
		@Override
		public TableCell<WatchedArtist, WatchedArtist> call(TableColumn<WatchedArtist, WatchedArtist> param) {
			return new TableCell<WatchedArtist, WatchedArtist>() {
				@Override
				protected void updateItem(WatchedArtist item, boolean empty) {
					super.updateItem(item, empty);
					textProperty().unbind();
					if (empty) {
						setText(null);
					} else {
						textProperty().bind(item.getLastRelease().dateProperty());
						textFillProperty().bind(new ObjectBinding<Paint>() {

							@Override
							protected Paint computeValue() {
								if (item.getLastRelease().isUpdated()) {
									return Color.RED;
								}
								return Color.BLACK;
							}
						});
					}
				}
			};
		}
	}

	public static final class AlbumNameCellFactory
			implements Callback<TableColumn<WatchedArtist, WatchedArtist>, TableCell<WatchedArtist, WatchedArtist>> {
		@Override
		public TableCell<WatchedArtist, WatchedArtist> call(TableColumn<WatchedArtist, WatchedArtist> param) {
			return new TableCell<WatchedArtist, WatchedArtist>() {
				@Override
				protected void updateItem(WatchedArtist item, boolean empty) {
					super.updateItem(item, empty);
					textProperty().unbind();
					if (empty) {
						setText(null);
					} else {
						textProperty().bind(item.getLastRelease().albumNameProperty());
						textFillProperty().bind(new ObjectBinding<Paint>() {

							@Override
							protected Paint computeValue() {
								if (item.getLastRelease().isUpdated()) {
									return Color.RED;
								}
								return Color.BLACK;
							}
						});
					}
				}
			};
		}
	}
}
