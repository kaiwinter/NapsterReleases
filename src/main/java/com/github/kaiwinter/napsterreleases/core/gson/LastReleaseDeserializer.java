package com.github.kaiwinter.napsterreleases.core.gson;

import java.lang.reflect.Type;

import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist.LastRelease;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * {@link JsonDeserializer} for {@link LastRelease}.
 */
public final class LastReleaseDeserializer implements JsonDeserializer<ObjectProperty<LastRelease>> {
	@Override
	public ObjectProperty<LastRelease> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject jsonObject = json.getAsJsonObject();
		JsonElement lastReleaseDate = jsonObject.get(LastReleaseSerializer.LAST_RELEASE_DATE_PROPERTY);
		JsonElement albumName = jsonObject.get(LastReleaseSerializer.ALBUM_NAME_PROPERTY);

		LastRelease lastRelease = new LastRelease();
		if (lastReleaseDate != null && albumName != null) {
			lastRelease.setDate(lastReleaseDate.getAsString());
			lastRelease.setAlbumName(albumName.getAsString());
		}
		return new SimpleObjectProperty<>(lastRelease);
	}
}
