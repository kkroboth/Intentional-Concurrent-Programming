package edu.unh.letsmeet.server;

import edu.unh.letsmeet.Utils;
import edu.unh.letsmeet.engine.HttpException;
import edu.unh.letsmeet.engine.Method;
import edu.unh.letsmeet.engine.Request;
import edu.unh.letsmeet.engine.RequestHandler;
import edu.unh.letsmeet.engine.Response;
import edu.unh.letsmeet.engine.Route;
import edu.unh.letsmeet.engine.ServerProvider;
import icp.core.ICP;
import icp.core.Permissions;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Handle case where trailing slash is missing but regex has it. Vice-versa too
public class ApiRequestHandler extends RequestHandlerDecorator {
  private static final Logger logger = Logger.getLogger("ApiRequestHandler");

  private final String rootPath;

  // TODO: Convert to list of pairs
  private final Map<Pattern, Route> routes;

  protected ApiRequestHandler(RequestHandler handler, Builder builder) {
    super(handler);
    this.rootPath = builder.rootPath;
    this.routes = builder.routes;

    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

  @Override
  public Response handleRequest(ServerProvider provider, Request request, Map<String, Object> meta) throws HttpException, IOException {
    String path = request.getUri().getPath();

    if (path.startsWith(rootPath)) {
      String routePath = path.substring(path.indexOf(rootPath, 0) + rootPath.length());
      // Iterate over all patterns
      for (Map.Entry<Pattern, Route> routeEntry : routes.entrySet()) {
        Pattern pattern = routeEntry.getKey();
        Matcher matcher = pattern.matcher(routePath);
        if (matcher.matches()) {
          // TODO: Retrieve url parameters
          try {
            // url params
            Map<String, String> namedGroups = getNamedGroups(matcher);
            String queryStr = request.getUri().getQuery();

            // url query
            Map<String, String> query;
            if (queryStr == null) query = Collections.emptyMap();
            else query = Utils.parseQueryString(queryStr);

            Route route = routeEntry.getValue();
            return route.accept(request.getMethod(), request.getUri().getPath(), namedGroups,
              query, request, meta, provider);
          } catch (IOException e) {
            throw new HttpException(500, e);
          }
        }
      }
    }

    return super.handleRequest(provider, request, meta);
  }

  /**
   * Recursive Builder allowing sub-routes.
   */
  public static class Builder {
    private final String rootPath;
    private final Map<Pattern, Route> routes;

    // Sub routes
    private Builder parent;

    public Builder(String rootPath) {
      this.rootPath = rootPath;
      this.routes = new HashMap<>();
    }

    public Builder addRoute(String path, Route route) {
      return addMethodRoute(null, path, route);
    }

    public Builder addMethodRoute(Method method, String path, Route route) {
      // add parent's root if subroute
      if (parent != null) {
        path = rootPath + path;
      }
      String finalPath = path;
      this.routes.put(Pattern.compile(path), (rmethod, rpath, params, query, request, meta, provider) -> {
        if (method != null && !rmethod.equals(method)) throw new HttpException(405);
        return route.accept(rmethod, finalPath, params, query, request, meta, provider);
      });
      return this;
    }

    /**
     * Create and enter sub-route builder appending <code>subRoute</code> to the current
     * rootPath.
     * <p>
     * Sub-routes are flattened to one list of routes. This allows multiple sub-routes
     * with the same name.
     *
     * @param subRoute appended path on current route (regex is allowed and parameters are added to
     *                 the matched route).
     * @return
     */
    public Builder enterSubRoute(String subRoute) {
      String subRoot;
      if (parent == null) subRoot = subRoute;
      else subRoot = rootPath + subRoute;
      Builder subBuilder = new Builder(subRoot);
      subBuilder.parent = this;
      return subBuilder;
    }

    /**
     * Pop sub-router and add all sub-routes to its parent.
     *
     * @return parent router
     */
    public Builder exitSubRoute() {
      Objects.requireNonNull(parent, "Did not enter a subroute");
      // Add all routes created in child to parent
      parent.routes.putAll(routes);
      return parent;
    }

    public ApiRequestHandler build(RequestHandler handler) {
      if (parent != null) {
        throw new IllegalStateException("Cannot build child router. Only top-most parent router may be built");
      }
      return new ApiRequestHandler(handler, this);
    }

    public ApiRequestHandler build() {
      return build(null);
    }
  }

  static {
    // HACK: Matcher does not allow retrieval of all named groups; i.e., pairs of group name to matched string.
    // Due to limited time of this project, reflections is used to get the private group map.
    //
    // TODO: Either figure out how or use another regex library.

    try {
      // parentPattern in Matcher class
      Field parentPattern = Matcher.class.getDeclaredField("parentPattern");
      parentPattern.setAccessible(true);

      // namedGroups() function Pattern class
      java.lang.reflect.Method namedGroups = Pattern.class.getDeclaredMethod("namedGroups");
      namedGroups.setAccessible(true);

      FIELD_MATCHER_PARENTPATTERN = parentPattern;
      METHOD_PATTERN_NAMEDGROUPS = namedGroups;
    } catch (NoSuchFieldException | NoSuchMethodException e) {
      logger.log(Level.SEVERE, "Reflection setup failed", e);
      throw new Error(e);
    }
  }

  /**
   * Get all named groups in regex matcher.
   * <p>
   * Uses reflection to bypass Java's Regex API limitations
   *
   * @param matcher Matcher
   * @return Map of Group name to Matched text
   */
  protected static Map<String, String> getNamedGroups(Matcher matcher) {
    try {
      Pattern parentPattern = (Pattern) FIELD_MATCHER_PARENTPATTERN.get(matcher);
      //noinspection unchecked
      Map<String, Integer> groups = (Map<String, Integer>) METHOD_PATTERN_NAMEDGROUPS.invoke(parentPattern);

      // Convert to map of group name and matched text
      Map<String, String> matchedGroups = new HashMap<>();
      for (Map.Entry<String, Integer> entry : groups.entrySet()) {
        matchedGroups.put(entry.getKey(), matcher.group(entry.getValue()));
      }

      return Collections.unmodifiableMap(matchedGroups);
    } catch (IllegalAccessException | InvocationTargetException | ClassCastException e) {
      logger.log(Level.SEVERE, "Reflection failed", e);
      throw new Error(e);
    }
  }

  private static final Field FIELD_MATCHER_PARENTPATTERN;
  private static final java.lang.reflect.Method METHOD_PATTERN_NAMEDGROUPS;

}
