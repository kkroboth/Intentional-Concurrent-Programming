package edu.unh.letsmeet.engine;

import icp.core.ICP;
import icp.core.Permissions;

import java.util.HashMap;
import java.util.Map;

public final class ContentTypes {
  private static final Map<String, ContentType> contentTypes;

  static {
    contentTypes = new HashMap<>();
    contentTypes.put("js", new ContentType("application/javascript", true));
    contentTypes.put("map", new ContentType("application/json", true));
    contentTypes.put("png", new ContentType("image/png", false));
    contentTypes.put("jpg", new ContentType("image/jpg", false));
  }

  public static ContentType getContentType(String extensionName) {
    return contentTypes.get(extensionName);
  }


  public static final class ContentType {
    private final String type;
    private final boolean useUtf;

    ContentType(String type, boolean useUtf) {
      this.type = type;
      this.useUtf = useUtf;
      ICP.setPermission(this, Permissions.getFrozenPermission());
    }

    public boolean useUtf() {
      return useUtf;
    }

    public String getType() {
      return type;
    }
  }

}
