package edu.unh.letsmeet.api;

/**
 * Reads api key from api properties defined in Props.
 * <p>
 * Will use the exact name of class (or enum in most cases) to lookup in properties
 * file. Use <code>API_KEY_NAME</code> public static String field to designate api name.
 */
public interface ReadableApiKey {

  void setApiKey(String key);
}
