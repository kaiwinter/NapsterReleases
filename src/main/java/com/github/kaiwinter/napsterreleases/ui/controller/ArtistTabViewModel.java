package com.github.kaiwinter.napsterreleases.ui.controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

public final class ArtistTabViewModel {

	private BooleanProperty loading = new SimpleBooleanProperty();

	private StringProperty name = new SimpleStringProperty();
	private StringProperty bio = new SimpleStringProperty();
	private StringProperty blubs = new SimpleStringProperty();
	private ObjectProperty<Image> image = new SimpleObjectProperty<>();

	public BooleanProperty loadingProperty() {
		return this.loading;
	}

	public StringProperty nameProperty() {
		return this.name;
	}

	public StringProperty bioProperty() {
		return this.bio;
	}

	public StringProperty blubsProperty() {
		return this.blubs;
	}

	public ObjectProperty<Image> imageProperty() {
		return this.image;
	}

	public void clear() {
		name.setValue(null);
		bio.setValue(null);
		blubs.setValue(null);
		image.setValue(null);
	}
}
