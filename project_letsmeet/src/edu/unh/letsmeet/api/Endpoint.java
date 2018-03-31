package edu.unh.letsmeet.api;

import com.krobothsoftware.commons.network.http.HttpRequest;

public interface Endpoint {

  /**
   * Domain of endpoint. Used for identifying endpoints.
   */
  String domain();

  /**
   * @return The base endpoint url
   */
  String endpoint();

  /**
   * Perform final modifications to request before sent off.
   *
   * @param request HttpRequest builder
   */
  default HttpRequest onRequest(HttpRequest request) {
    return request;
  }
}
