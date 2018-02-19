package edu.unh.letsmeet.engine;

import java.util.HashMap;
import java.util.Map;

@FunctionalInterface
public interface Route {

  Response accept(Method method, String path, Request request, Map<String, Object> meta,
                  ServerProvider provider) throws HttpException;

  final class Builder {
    private final Map<String, Route> routes;

    public Builder() {
      routes = new HashMap<>();
    }

    public Builder addRoute(String path, Route route) {
      this.routes.put(path.toLowerCase(), route);
      return this;
    }

    public Map<String, Route> done() {
      return routes;
    }
  }
}
