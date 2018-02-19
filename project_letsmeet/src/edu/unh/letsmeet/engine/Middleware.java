package edu.unh.letsmeet.engine;

import java.util.Map;

public interface Middleware {

  /**
   * Handle request and either return Response or null to continue on
   *
   * @param provider
   * @param meta
   * @param request
   * @return Response to end or null to continue
   */
  Response onRequest(ServerProvider provider, Map<String, Object> meta, Request request) throws HttpException;

  /**
   * Handle final stage and making changes to response
   *
   * @param provider
   * @param meta
   * @param response
   * @param request
   */
  void onResponse(ServerProvider provider, Map<String, Object> meta, Response.Builder response, Request request);

}
