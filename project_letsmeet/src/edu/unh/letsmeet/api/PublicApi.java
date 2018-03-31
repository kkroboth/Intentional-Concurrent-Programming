package edu.unh.letsmeet.api;

import icp.core.ICP;
import icp.core.Permissions;

/**
 * Public (free) apis with not access token
 */
public enum PublicApi implements Endpoint {
  COUNTRY("restcountries.eu", "https://restcountries.eu/rest/v2/");


  private final String domain;
  private final String endpoint;

  PublicApi(String domain, String endpoint) {
    this.domain = domain;
    this.endpoint = endpoint;
    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  @Override
  public String domain() {
    return domain;
  }

  @Override
  public String endpoint() {
    return endpoint;
  }
}
