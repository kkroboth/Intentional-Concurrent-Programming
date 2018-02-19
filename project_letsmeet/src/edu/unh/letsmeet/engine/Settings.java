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
  // unmodifiableMap. Volatile as one thread may populate the
  // settings, and afterwards another will read them.
  //
  // Note, no one is allowed to read the settings before frozen.

  private volatile Map<String, Object> settings;

  Settings() {
    settings = new HashMap<>();

    // Should it be thread safe???
    // Is frozen after freeze() is called, but they are all method calls anyways
    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

  /**
   * Called by the server before starting.
   */
  synchronized void freeze() {
    // Someone could call this multiple times, but the map is still frozen.
    settings = Collections.unmodifiableMap(settings);
  }

  public <V> void set(String key, V value) {
    settings.put(key, value);
  }

  public <V> V get(String key) {
    //noinspection unchecked
    if (!settings.containsKey(key))
      throw new IllegalStateException("Settings: " + key + " accessed but has not value set");
    return (V) settings.get(key);
  }

}
