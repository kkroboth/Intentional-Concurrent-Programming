package edu.unh.letsmeet.engine;

import java.util.HashMap;
import java.util.Map;

public final class Status {
  private static final Map<Integer, String> phrases;

  static {
    phrases = new HashMap<>();
    phrases.put(200, "OK");
    phrases.put(204, "No Content");
    phrases.put(302, "Found");
    phrases.put(400, "Bad Request");
    phrases.put(401, "Unauthorized");
    phrases.put(403, "Forbidden");
    phrases.put(404, "Not Found");
    phrases.put(405, "Method Not Allowed");
    phrases.put(413, "Request Entity Too Large");
    phrases.put(500, "Internal Server Error");
  }

  public static String reasonPhrase(int status) {
    String phrase = phrases.get(status);
    if (phrase == null) throw new IllegalArgumentException("Status " + status + " has no registered phrase");
    return phrase;
  }
}
