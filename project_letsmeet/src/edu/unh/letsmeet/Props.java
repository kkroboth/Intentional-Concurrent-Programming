package edu.unh.letsmeet;

import icp.core.ICP;
import icp.core.Permissions;
import icp.wrapper.ICPProxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Singleton class that holds server properties (read-only).
 * <p>
 * Reads from <em>server.properties</em>.
 */
public final class Props {
  private final Properties props;

  // Avoid double check idiom
  private static class InstanceWrapper {
    private static final Props INSTANCE = new Props();

  }

  public static Props getInstance() {
    return InstanceWrapper.INSTANCE;
  }

  private Props() {
    Properties props = null;
    try {
      props = loadProps(new FileInputStream(System.getProperty("edu.unh.letsmeet.config.file")));
    } catch (IOException e) {
      // TODO: Use our own logger
      e.printStackTrace();
    }

    this.props = props;
    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  public Path getProjectPath() {
    return Paths.get("./");
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

  public long getMaxContentLength() {
    Objects.requireNonNull(this.props);
    return Long.parseLong(props.getProperty("max_content_length"));
  }

  public Path getPagesDirectory() {
    Objects.requireNonNull(this.props);
    String path = props.getProperty("pages_directory");
    Objects.requireNonNull(path, "pages_directory property is not set");
    return Paths.get(path);
  }

  public Path getStaticDirectory() {
    Objects.requireNonNull(this.props);
    String path = props.getProperty("static_directory");
    Objects.requireNonNull(path, "static_directory property is not set");
    return Paths.get(path);
  }

  public Path getStorageDirectory() {
    Objects.requireNonNull(this.props);
    String path = props.getProperty("storage_directory");
    Objects.requireNonNull(path, "storage_directory property is not set");
    return Paths.get(path);
  }

  public Path getNodemodulesDirectory() {
    Objects.requireNonNull(this.props);
    String path = props.getProperty("nodemodules_directory");
    Objects.requireNonNull(path, "nodemodules_directory property is not set");
    return Paths.get(path);
  }

  /**
   * Reads and returns the following api keys:
   * <ul>
   * <li>key_weather (openweathermap)</li>
   * <li>key_restaurants (Zomato)</li>
   * <li>key_news (newsapi)</li>
   * <li>key_events (TicketMaster)</li>
   * </ul>
   */
  public Map<String, String> readApiKeys() throws IOException {
    Objects.requireNonNull(this.props);
    String strPath = props.getProperty("api_properties_file");
    Objects.requireNonNull(strPath, "api_properties_file property is not set");
    Path path = Paths.get(strPath);
    Properties props = new Properties();
    props.load(Files.newInputStream(path));

    Map<String, String> keys = new HashMap<>();
    Stream.of("key_weather", "key_restaurants", "key_news", "key_events")
      .forEach(key -> keys.put(key, props.getProperty(key)));

    //noinspection unchecked
    return ICPProxy.newFrozenInstance(Map.class, Collections.unmodifiableMap(keys));
  }

  private Properties loadProps(InputStream stream) throws IOException {
    Properties props = new Properties();
    props.load(stream);
    return props;
  }
}
