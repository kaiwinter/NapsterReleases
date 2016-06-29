package com.github.kaiwinter.napsterreleases.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * Loads the Rhapsody API key and secret from a Properties file. The file (apikey.properties) have to be located in the
 * same package as this class.
 */
public final class RhapsodyApiKeyProperties {

   private static final String APIKEY_PROPERTIES_FILE = "apikey.properties";
   private static final String PROPERTY_API_KEY = "rhapsody.api.key";
   private static final String PROPERTY_API_SECRET = "rhapsody.api.secret";

   private static final String MISSING_PROPERTY_FILE = "Could not load API key and API secret. No ''{0}'' in ''{1}''";
   private static final String MISSING_PROPERTY = "{0} doesn''t contain key ''{1}''.";

   public final String apiKey;
   public final String apiSecret;

   /**
    * Constructs a new {@link RhapsodyApiKeyProperties} and loads the API key and secret.
    *
    * @throws IOException
    *            if the properties file does not exist or if it could not be read
    */
   public RhapsodyApiKeyProperties() throws IOException {
      Properties properties = new Properties();
      try (InputStream stream = RhapsodyApiKeyProperties.class.getResourceAsStream(APIKEY_PROPERTIES_FILE)) {
         if (stream == null) {
            String message = MessageFormat.format(MISSING_PROPERTY_FILE, APIKEY_PROPERTIES_FILE,
               RhapsodyApiKeyProperties.class.getPackage().getName());
            throw new IOException(message);
         }
         properties.load(stream);
         apiKey = properties.getProperty(PROPERTY_API_KEY);
         apiSecret = properties.getProperty(PROPERTY_API_SECRET);

         if (apiKey == null) {
            String message = MessageFormat.format(MISSING_PROPERTY, APIKEY_PROPERTIES_FILE, PROPERTY_API_KEY);
            throw new IOException(message);
         }
         if (apiSecret == null) {
            String message = MessageFormat.format(MISSING_PROPERTY, APIKEY_PROPERTIES_FILE, PROPERTY_API_SECRET);
            throw new IOException(message);
         }
      }
   }

}
