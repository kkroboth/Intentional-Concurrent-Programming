package edu.unh.letsmeet.api;

import com.google.gson.JsonElement;

import java.io.IOException;
import java.io.InputStream;

/**
 * Extracts an API endpoint response to JSON to be
 * send back to the client.
 */
@FunctionalInterface
public interface Extractor {

  JsonElement parseContent(InputStream content) throws IOException;
}
