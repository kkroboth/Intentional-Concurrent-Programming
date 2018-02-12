package edu.unh.letsmeet.engine;

import java.util.HashMap;
import java.util.Map;

public final class ContentTypes {
  private static final Map<String, String> contentTypes;

  static {
    contentTypes = new HashMap<>();
    contentTypes.put("js", "application/javascript");
  }

  public static String getContentType(String extensionName) {
    return contentTypes.get(extensionName);
  }

}
