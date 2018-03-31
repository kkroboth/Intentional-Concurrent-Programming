package edu.unh.letsmeet.api;

import com.krobothsoftware.commons.network.http.HttpRequest;
import icp.core.ICP;
import icp.core.Permissions;

import java.util.Objects;

/**
 * Api that require authentication or access tokens.
 */
public enum RequireAuthApi implements AuthEndpoint, ReadableApiKey {
  WEATHER("api.openweathermap.org", "https://api.openweathermap.org/data/2.5/", "APPID"),

  NEWS("newsapi.org", "https://newsapi.org/v2/", "apiKey"),

  RESTAURANTS("developers.zomato.com", "https://developers.zomato.com/api/v2.1/", "user-key") {
    @Override
    public void insertKey(URLBuilder builder) {
      // ignored -- key is added in header
    }

    @Override
    public HttpRequest onRequest(HttpRequest request) {
      return request.header(this.keyName, this.key);
    }
  },

  EVENTS("api.ticketmaster.com", "https://app.ticketmaster.com/discovery/v2/", "apikey");

  // Effectively final
  String key;
  final String domain;
  final String endpoint;
  final String keyName;

  RequireAuthApi(String domain, String endpoint, String keyName) {
    this.domain = domain;
    this.endpoint = endpoint;
    this.keyName = keyName;
  }


  @Override
  public String domain() {
    return domain;
  }

  @Override
  public String endpoint() {
    return endpoint;
  }

  @Override
  public void insertKey(URLBuilder builder) {
    Objects.requireNonNull(key, "API key was not set for: " + name());
    builder.query(keyName, key);
  }

  @Override
  public void setApiKey(String key) {
    this.key = key;
    ICP.setPermission(this, Permissions.getFrozenPermission());
  }


}
