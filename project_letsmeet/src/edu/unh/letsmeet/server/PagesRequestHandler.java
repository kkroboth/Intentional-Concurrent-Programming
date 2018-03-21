package edu.unh.letsmeet.server;

import edu.unh.letsmeet.engine.HttpException;
import edu.unh.letsmeet.engine.Request;
import edu.unh.letsmeet.engine.RequestHandler;
import edu.unh.letsmeet.engine.Response;
import edu.unh.letsmeet.engine.Route;
import edu.unh.letsmeet.engine.ServerProvider;
import icp.core.ICP;
import icp.core.Permissions;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.logging.Logger;

public class PagesRequestHandler extends RequestHandlerDecorator {
  private static final Logger logger = Logger.getLogger("PagesRequestHandler");

  public static final String SETTING_PAGES_DIRECTORY = "directory.pagesrequesthandler";

  private final Map<String, Route> routes;

  public PagesRequestHandler(Map<String, Route> routes, RequestHandler delegate) {
    super(delegate);
    this.routes = routes;

    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  public PagesRequestHandler(Map<String, Route> routes) {
    this(routes, null);
  }

  @Override
  public Response handleRequest(ServerProvider provider, Request request, Map<String, Object> meta) throws HttpException, IOException {
    URI uri = request.getUri();
    String path = uri.getPath().toLowerCase();
    Route route = routes.get(path);
    if (route != null) {
      return route.accept(request.getMethod(), request.getUri().getPath(), request, meta, provider);
    }

    return super.handleRequest(provider, request, meta);
  }
}
