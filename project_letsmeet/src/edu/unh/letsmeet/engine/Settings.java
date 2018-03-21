package edu.unh.letsmeet.engine;

import icp.core.ICP;
import icp.core.Permissions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Meant to provide settings to all request handlers and operations of server.
 * <p>
 * Settings are immutable once frozen.
 */
public final class Settings {
  // Strategy:
  //
  // Once frozen, the settings map will be wrapped in an
  // unmodifiableMap.

  private Map<String, Object> settings;

  public Settings() {
    settings = new HashMap<>();
    // Permission: private util frozen
  }

  /**
   * Called by the server before starting.
   */
  void freeze() {
    settings = Collections.unmodifiableMap(settings);
    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  public <V> void set(String key, V value) {
    settings.put(key, value);
  }

  public <V> V get(String key) {
    //noinspection unchecked
    if (!settings.containsKey(key))
      throw new IllegalStateException("Settings: " + key + " accessed but has not value set");
    //noinspection unchecked
    return (V) settings.get(key);
  }

}
