package edu.unh.letsmeet.engine;

import icp.core.ICP;
import icp.core.Permissions;

public final class Cookie {
  private String key;
  private String value;

  public Cookie(String key, String value) {
    this.key = key;
    this.value = value;
    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }
}
