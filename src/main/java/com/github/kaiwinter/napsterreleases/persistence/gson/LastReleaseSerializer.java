package com.github.kaiwinter.napsterreleases.persistence.gson;

import java.lang.reflect.Type;

import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist.LastRelease;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import javafx.beans.property.ObjectProperty;

/**
 * {@link JsonSerializer} for {@link LastRelease}.
 */
public final class LastReleaseSerializer implements JsonSerializer<ObjectProperty<LastRelease>> {
	public static final String LAST_RELEASE_DATE_PROPERTY = "lastReleaseDate";
	public static final String ALBUM_NAME_PROPERTY = "albumName";

	@Override
	public JsonElement serialize(ObjectProperty<LastRelease> src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(LAST_RELEASE_DATE_PROPERTY, src.getValue().getDate());
		jsonObject.addProperty(ALBUM_NAME_PROPERTY, src.getValue().getAlbumName());
		return jsonObject;
	}
}
