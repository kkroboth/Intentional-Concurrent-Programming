package edu.unh.letsmeet.engine;

/**
 * Interface used for accessing server properties and settings.
 */
public interface ServerProvider {

  int getPort();

  String getHost();

  Settings getSettings();

}
