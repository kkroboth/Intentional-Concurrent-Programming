package edu.unh.letsmeet;

import icp.core.ICP;
import icp.core.Permissions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Singleton class that holds server properties (read-only).
 * <p>
 * Reads from <em>server.properties</em>.
 */
public final class Props {
  private final Properties props;

  // Avoid double check idiom
  private static class InstanceWrapper {
    private static final Props instance = new Props();

  }

  public static Props getInstance() {
    return InstanceWrapper.instance;
  }

  private Props() {
    Properties props = null;
    try {
      props = loadProps(getClass().getClassLoader().getResourceAsStream("server.properties"));
    } catch (IOException e) {
      // TODO: Use our own logger
      e.printStackTrace();
    }

    this.props = props;
    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  public String getHost() {
    Objects.requireNonNull(this.props);
    return props.getProperty("host");
  }

  public int getPort() {
    Objects.requireNonNull(this.props);
    return Integer.valueOf(props.getProperty("port"));
  }

  public int getServerThreads() {
    Objects.requireNonNull(this.props);
    String threads = props.getProperty("server_threads");
    return threads != null ? Integer.valueOf(threads) : Runtime.getRuntime().availableProcessors();
  }

  private Properties loadProps(InputStream stream) throws IOException {
    Properties props = new Properties();
    props.load(stream);
    return props;
  }
}
