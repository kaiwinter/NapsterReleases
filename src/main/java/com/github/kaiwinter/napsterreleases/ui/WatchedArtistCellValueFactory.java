package com.github.kaiwinter.napsterreleases.ui;

import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist;

import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

/**
 * Collects CellFactories and CellValueFactories for the {@link WatchedArtist}.
 */
public final class WatchedArtistCellValueFactory {

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

	public static final class ArtistNameCellFactory extends TableCell<WatchedArtist, WatchedArtist> {
		@Override
		protected void updateItem(WatchedArtist item, boolean empty) {
			super.updateItem(item, empty);
			textProperty().unbind();
			if (empty) {
				setText(null);
			} else {
				setText(item.getArtist().name);
				textFillProperty().bind(item.getLastRelease().textColorProperty());
			}
		}
	}

	public static final class LastReleaseCellFactory extends TableCell<WatchedArtist, WatchedArtist> {
		@Override
		protected void updateItem(WatchedArtist item, boolean empty) {
			super.updateItem(item, empty);
			textProperty().unbind();
			if (empty) {
				setText(null);
			} else {
				textProperty().bind(item.getLastRelease().dateProperty());
				textFillProperty().bind(item.getLastRelease().textColorProperty());
			}
		}
	}

	public static final class AlbumNameCellFactory extends TableCell<WatchedArtist, WatchedArtist> {
		@Override
		protected void updateItem(WatchedArtist item, boolean empty) {
			super.updateItem(item, empty);
			textProperty().unbind();
			if (empty) {
				setText(null);
			} else {
				textProperty().bind(item.getLastRelease().albumNameProperty());
				textFillProperty().bind(item.getLastRelease().textColorProperty());
			}
		}
	}
}
