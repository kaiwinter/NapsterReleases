package com.github.kaiwinter.napsterreleases.ui;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

/**
 * CellFactory which calls a method after a double click.
 */
public class DoubleClickListenerCellFactory<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>> {

	/**
	 * {@link FunctionalInterface} to pass a method call like a method parameter.
	 */
	@FunctionalInterface
	public interface MethodInterface {
		void call();
	}

	private final MethodInterface method;

	/**
	 * Constructs a new {@link DoubleClickListenerCellFactory}.
	 *
	 * @param method
	 *            the method which is called after a double click was detected
	 */
	public DoubleClickListenerCellFactory(MethodInterface method) {
		this.method = method;
	}

	@Override
	public TableCell<S, T> call(TableColumn<S, T> param) {
		TableCell<S, T> tableCell = new TextFieldTableCell<>();
		tableCell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
			if (event.getClickCount() == 2) {
				method.call();
			}
		});
		return tableCell;
	}
}