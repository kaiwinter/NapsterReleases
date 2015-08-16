package com.github.kaiwinter.napsterreleases;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads the Rhapsody API key and secret from a Properties file. The file (apikey.properties) have to be located in the same package as this
 * class.
 */
public final class RhapsodyApiKeyConfig {

	public final String apiKey;
	public final String apiSecret;

	/**
	 * Constructs a new {@link RhapsodyApiKeyConfig} and loads the API key and secret.
	 *
	 * @throws IOException
	 *             if the properties file does not exist or if it could not be read
	 */
	public RhapsodyApiKeyConfig() throws IOException {
		Properties properties = new Properties();
		try (InputStream stream = RhapsodyApiKeyConfig.class.getResourceAsStream("apikey.properties")) {
			if (stream == null) {
				throw new IOException("Could not load API key and API secret. No 'apikey.properties' in '"
						+ RhapsodyApiKeyConfig.class.getPackage().getName() + "'");
			}
			properties.load(stream);
			apiKey = properties.getProperty("rhapsody.api.key");
			apiSecret = properties.getProperty("rhapsody.api.secret");
		}
	}

}
