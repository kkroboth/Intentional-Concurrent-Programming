package edu.unh.letsmeet.engine;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@FunctionalInterface
public interface Route {

  Response accept(Method method, String path, Map<String, String> params, Map<String, String> query, Request request, Map<String, Object> meta,
                  ServerProvider provider) throws HttpException, IOException;

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

  default Response accept(Method method, String path, Request request, Map<String, Object> meta,
                          ServerProvider provider) throws HttpException, IOException {
    return accept(method, path, Collections.emptyMap(), Collections.emptyMap(), request, meta, provider);
  }
}
