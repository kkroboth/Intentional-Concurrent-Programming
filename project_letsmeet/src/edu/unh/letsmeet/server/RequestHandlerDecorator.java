package edu.unh.letsmeet.server;

import edu.unh.letsmeet.engine.HttpException;
import edu.unh.letsmeet.engine.Request;
import edu.unh.letsmeet.engine.RequestHandler;
import edu.unh.letsmeet.engine.Response;
import edu.unh.letsmeet.engine.ServerProvider;

import java.io.IOException;
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
  public Response.Builder handleRequest(ServerProvider provider, Request request, Map<String, Object> meta) throws HttpException, IOException {
    if (handler != null) return handler.handleRequest(provider, request, meta);
    else throw new HttpException(404, "No matching request: " + request.toString());
  }
}
