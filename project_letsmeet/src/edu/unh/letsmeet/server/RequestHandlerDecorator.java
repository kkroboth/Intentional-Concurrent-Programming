package edu.unh.letsmeet.server;

import edu.unh.letsmeet.engine.Request;
import edu.unh.letsmeet.engine.RequestHandler;
import edu.unh.letsmeet.engine.Response;

public abstract class RequestHandlerDecorator implements RequestHandler {
  private final RequestHandler handler;

  public RequestHandlerDecorator(RequestHandler handler) {
    this.handler = handler;
  }

  public RequestHandlerDecorator() {
    this(null);
  }

  @Override
  public Response handleRequest(Request request) {
    return handler != null ? handler.handleRequest(request)
      : new Response.Builder(404).build();
  }
}
