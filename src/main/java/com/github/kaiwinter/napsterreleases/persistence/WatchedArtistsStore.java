package com.github.kaiwinter.napsterreleases.persistence;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.napsterreleases.persistence.gson.LastReleaseDeserializer;
import com.github.kaiwinter.napsterreleases.persistence.gson.LastReleaseSerializer;
import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javafx.beans.property.ObjectProperty;

/**
 * Persists the watched artists with their latest album release. The FX Bean {@link WatchedArtist} is persisted directly
 * therefore the {@link LastReleaseSerializer} and {@link LastReleaseDeserializer} are necessary.
 */
@Singleton
public class WatchedArtistsStore {
   private static final Logger LOGGER = LoggerFactory.getLogger(WatchedArtistsStore.class.getSimpleName());
   private static final String WATCHLIST_FILE = "watchedartists.json";

   /**
    * Loads the watched artists from the file.
    * 
    * @return the watched artists
    */
   public Set<WatchedArtist> loadWatchedArtists() {
      File file = new File(WATCHLIST_FILE);
      if (!file.exists()) {
         return new HashSet<>();
      }

      try {
         String string = new String(Files.readAllBytes(file.toPath()));
         Type collectionType = new TypeToken<Set<WatchedArtist>>() {
         }.getType();

         Gson gson = new GsonBuilder().registerTypeAdapter(ObjectProperty.class, new LastReleaseDeserializer())
            .create();
         Set<WatchedArtist> fromJson = gson.fromJson(string, collectionType);
         return fromJson;
      } catch (IOException e) {
         LOGGER.error(e.getMessage(), e);
         return new HashSet<>();
      }
   }

   /**
    * Saves the passed watched artists to the file.
    * 
    * @param watchedArtists
    *           the watched artists, not <code>null</code>
    */
   public void saveWatchedArtists(Set<WatchedArtist> watchedArtists) {
      Gson gson = new GsonBuilder().registerTypeAdapter(ObjectProperty.class, new LastReleaseSerializer()).create();
      String json = gson.toJson(watchedArtists);
      try {
         Files.write(new File(WATCHLIST_FILE).toPath(), json.getBytes(), StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING);
      } catch (IOException e) {
         LOGGER.error(e.getMessage(), e);
      }
   }
}
