package edu.unh.letsmeet.server;

import edu.unh.letsmeet.engine.HttpException;
import edu.unh.letsmeet.engine.Request;
import edu.unh.letsmeet.engine.RequestHandler;
import edu.unh.letsmeet.engine.Response;
import edu.unh.letsmeet.engine.ServerProvider;

import java.util.Map;

public abstract class RequestHandlerDecorator implements RequestHandler {
  private final RequestHandler handler;

  public RequestHandlerDecorator(RequestHandler handler) {
    this.handler = handler;
  }

  public RequestHandlerDecorator() {
    this(null);
  }

  @Override
  public Response handleRequest(ServerProvider provider, Request request, Map<String, Object> meta) throws HttpException {
    return handler != null ? handler.handleRequest(provider, request, meta)
      : new Response.Builder(404).build();
  }
}
