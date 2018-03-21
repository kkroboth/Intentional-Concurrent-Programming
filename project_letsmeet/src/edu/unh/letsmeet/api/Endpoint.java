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
   * Add api key to url
   *
   * @param builder builder to insert api key to
   * @return url with key inserted
   */
  void insertKey(URLBuilder builder);

  /**
   * Perform final modifications to request before sent off.
   *
   * @param request HttpRequest builder
   */
  default HttpRequest onRequest(HttpRequest request) {
    return request;
  }
}
