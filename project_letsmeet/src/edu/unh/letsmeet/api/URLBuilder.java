package edu.unh.letsmeet.api;

import icp.wrapper.ICPProxy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class to build urls by appending parameters and paths.
 */
public final class URLBuilder {
  private final List<String> paths;
  private final List<String> queries;
  private Endpoint endpoint;

  public static URLBuilder empty() {
    return new URLBuilder();
  }

  public static URLBuilder ofEndpoint(Endpoint endpoint) {
    return new URLBuilder().endpoint(endpoint);
  }

  private URLBuilder() {
    //noinspection unchecked
    paths = ICPProxy.newPrivateInstance(List.class, new ArrayList<>());
    //noinspection unchecked
    queries = ICPProxy.newPrivateInstance(List.class, new ArrayList<>());
  }

  /**
   * Set api endpoint origin.
   */
  public URLBuilder endpoint(Endpoint endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  /**
   * Append path to url. Will split up multiple paths.
   */
  public URLBuilder path(String path) {
    Collections.addAll(paths, path.split("/"));
    return this;
  }

  public URLBuilder query(String name, String value) {
    queries.add(name + "=" + value);
    return this;
  }

  public String buildString() {
    endpoint.insertKey(this);
    StringBuilder sb = new StringBuilder();
    sb.append(endpoint.endpoint());
    sb.append(String.join("/", paths));
    if (!queries.isEmpty()) {
      sb.append("?")
        .append(String.join("&", queries));
    }

    return sb.toString();
  }

  public URL buildURL() throws MalformedURLException {
    return new URL(buildString());
  }


}
