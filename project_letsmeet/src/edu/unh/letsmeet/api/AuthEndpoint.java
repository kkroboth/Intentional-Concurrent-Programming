package edu.unh.letsmeet.api;

/**
 * API endpoint that requires authentication.
 */
public interface AuthEndpoint extends Endpoint {

  /**
   * Add api key to url
   *
   * @param builder builder to insert api key to
   * @return url with key inserted
   */
  void insertKey(URLBuilder builder);

}
