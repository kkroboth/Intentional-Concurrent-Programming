package edu.unh.letsmeet.server;

import edu.unh.letsmeet.engine.HttpException;
import edu.unh.letsmeet.engine.Request;
import edu.unh.letsmeet.engine.RequestHandler;
import edu.unh.letsmeet.engine.Response;
import edu.unh.letsmeet.engine.Route;
import edu.unh.letsmeet.engine.ServerProvider;
import icp.core.ICP;
import icp.core.Permissions;

import java.util.HashMap;
import java.util.Map;

public class ApiRequestHandler extends RequestHandlerDecorator {
  private final String rootPath;
  private final Map<String, Route> routes;

  protected ApiRequestHandler(RequestHandler handler, Builder builder) {
    super(handler);
    this.rootPath = builder.rootPath;
    this.routes = builder.routes;

    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

  @Override
  public Response handleRequest(ServerProvider provider, Request request, Map<String, Object> meta) throws HttpException {
    String path = request.getUri().getPath();

    if (path.startsWith(rootPath)) {
      String routePath = path.substring(path.indexOf(rootPath, 0) + rootPath.length());
      Route route = routes.get(routePath);
      if (route != null) {
        return route.accept(request.getMethod(), request.getUri().getPath(), request, meta, provider);
      }
    }

    return super.handleRequest(provider, request, meta);
  }

  public static class Builder {
    private final String rootPath;
    private final Map<String, Route> routes;

    public Builder(String rootPath) {
      this.rootPath = rootPath;
      this.routes = new HashMap<>();
    }

    public Builder addRoute(String path, Route route) {
      this.routes.put(path, route);
      return this;
    }

    public ApiRequestHandler build(RequestHandler handler) {
      return new ApiRequestHandler(handler, this);
    }

    public ApiRequestHandler build() {
      return build(null);
    }
  }


}
