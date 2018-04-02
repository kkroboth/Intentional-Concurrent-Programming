package edu.unh.letsmeet;

import ch.hsr.geohash.GeoHash;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.krobothsoftware.commons.network.http.HttpHelper;
import com.krobothsoftware.commons.network.http.HttpResponse;
import com.krobothsoftware.commons.network.http.cookie.CookieManager;
import com.krobothsoftware.commons.util.StringUtils;
import edu.unh.letsmeet.api.ApiHttpRequest;
import edu.unh.letsmeet.api.ApiRegistry;
import edu.unh.letsmeet.api.extractors.EventsExtractor;
import edu.unh.letsmeet.api.extractors.JsonExtractor;
import edu.unh.letsmeet.api.extractors.RestaurantsExtractor;
import edu.unh.letsmeet.api.extractors.WeatherHourlyExtractor;
import edu.unh.letsmeet.engine.*;
import edu.unh.letsmeet.engine.security.SessionManager;
import edu.unh.letsmeet.server.PagesRequestHandler;
import edu.unh.letsmeet.server.PatternRequestHandler;
import edu.unh.letsmeet.server.StaticFilesRequestHandler;
import edu.unh.letsmeet.server.routes.HtmlRoute;
import edu.unh.letsmeet.storage.TravelLocation;
import edu.unh.letsmeet.storage.UniversalStorage;
import edu.unh.letsmeet.users.DatabaseHelper;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.CountDownLatch;
import icp.lib.ICPExecutorService;
import icp.lib.ICPExecutors;
import icp.wrapper.ICPProxy;
import icp.wrapper.ICPWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.unh.letsmeet.engine.Method.GET;

public class Main implements ServiceProvider {
  private static final Logger logger = Logger.getLogger("Main");

  // Guarded-by: Main instance
  private HttpServer httpServer;
  // Guarded-by: Main instance
  private SessionManager sessions;

  // Thread-safe
  private UniversalStorage universalStorage;

  private ApiRegistry apiRegistry;
  private Props props;
  private Settings settings;

  // Used to initiate http requests (not used in server)
  private HttpHelper httpHelper;

  private boolean shuttingDown;

  private final ICPExecutorService requestExecutor;
  private final Cache<Integer, String> aggregateCache =
    Caffeine.newBuilder()
      .maximumSize(100)
      .expireAfterWrite(Duration.ofMinutes(60))
      .build();


  public Main() {
    requestExecutor = ICPExecutors.newICPExecutorService(
      Executors.newCachedThreadPool()
    );
    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

  public synchronized void start() throws IOException {
    props = Props.getInstance();

    // HttpHelper used for initiated outside http requests
    httpHelper = new HttpHelper();
    httpHelper.setCookieManager(readCookieManagerFromStorage(props));

    // Setup storage
    universalStorage = new UniversalStorage();
    universalStorage.parseAndSetTravelMap(
      props.getProjectPath().resolve(Constants.ASSET_CITIES).toAbsolutePath()
    );

    // Middleware
    sessions = SessionManager.readFromStorage(props.getStorageDirectory().resolve(Constants.STORAGE_SESSIONS));
    sessions.requireAuthorization("/api");

    //noinspection unchecked
    List<Middleware> middlewares = ICPProxy.newPrivateInstance(List.class, new ArrayList<>());
    middlewares.add(sessions);

    // RequestHandlers use the decorator pattern, messy but gets job done
    httpServer = new HttpServer(this,
      ICPExecutors.newICPExecutorService(Executors.newCachedThreadPool()),
      new StaticFilesRequestHandler(
        createApiHandler(
          createPageRoutes(null)
        )
      ), middlewares);
    DatabaseHelper noop = DatabaseHelper.getInstance(); // Starts database

    // Add settings
    settings = new Settings();
    settings.set(PagesRequestHandler.SETTING_PAGES_DIRECTORY, props.getPagesDirectory());
    // Add api keys to settings
    settings.set(Constants.SETTING_APIKEYS, props.readApiKeys());

    // Setup apis
    apiRegistry = new ApiRegistry(httpHelper);
    apiRegistry.completeRegistration(settings);


    // Add static directories
    List<Path> dirs = new ArrayList<>();
    dirs.add(props.getStaticDirectory());
    dirs.add(props.getNodemodulesDirectory());
    // Add frontend static directory
    dirs.add(props.getProjectPath().resolve("frontend/static/"));

    settings.set(StaticFilesRequestHandler.SETTING_STATIC_DIRECTORIES, dirs);
    settings.set(StaticFilesRequestHandler.SETTING_URL_PATH, "/static/");

    // Asset directory
    Path value = props.getProjectPath().resolve("assets").toAbsolutePath();
    settings.set(Constants.ASSETS_DIR, value);

    // start the server!
    httpServer.start();
  }

  public synchronized void stop() {
    if (shuttingDown) return;
    shuttingDown = true;
    try {
      requestExecutor.shutdown();
      httpServer.stop();
      DatabaseHelper.getInstance().close();
      SessionManager.saveToStorage(sessions, props.getStorageDirectory().resolve(Constants.STORAGE_SESSIONS));
      // Purge session cookies and save to disk
      httpHelper.getCookieManager().purgeExpired(true);
      saveCookieManagerToStorage(httpHelper.getCookieManager(), props);
    } catch (SQLException | IOException e) {
      logger.log(Level.SEVERE, e.toString(), e);
    }
  }

  private PatternRequestHandler createApiHandler(RequestHandler handler) {
    return new PatternRequestHandler.Builder("/api/")

      // Map sub routes
      .enterSubRoute("map/")
      .addMethodRoute(GET, "points", (method, path, params, query, request, meta, provider) -> {
        // Limit results by percentage
        // Yes, not great design. A better one is to limit the amount of points in a given
        // radius.
        float filter;
        boolean useChunked = false;
        if (request.getUri().getQuery() != null) {
          try {
            Map<String, String> queryParams = Utils.parseQueryString(request.getUri().getQuery());
            if (queryParams.containsKey("filter")) {
              filter = Float.parseFloat(queryParams.get("filter"));
              if (filter < 0.0F) throw new HttpException(400);
            }

            if (queryParams.containsKey("chunked")) useChunked = true;
          } catch (IOException | NumberFormatException e) {
            throw new HttpException(400);
          }
        }


        Response.Builder response = Response.create(200)
          .header("Content-Type", "application/json; charset=utf8");

        if (useChunked) {
          return response.streamChunked(output -> universalStorage.writeAllTravelLocations(output));
        } else {
          try {
            universalStorage.writeAllTravelLocations(response);
            return response;
          } catch (IOException e) {
            throw new HttpException(500, e);
          }
        }

      })
      .addMethodRoute(GET, "point/(?<id>\\d+)", (method, path, params, query, request, meta, provider) -> {
        TravelLocation location = universalStorage.getTravelLocation(Integer.parseInt(params.get("id")));
        if (location == null) throw new HttpException(404);

        Gson gson = new Gson();
        String json = gson.toJson(location);

        return Response.create(200)
          .json(json);
      })
      .exitSubRoute() // map

      // Source sub routes
      .enterSubRoute("source/")
      // Weather
      .enterSubRoute("weather/")
      .addMethodRoute(GET, "location", ((method, path, params, query, request, meta, provider) -> {
        // TODO: Cache weather api calls (updated every 10 minutes)
        // TODO: Build cache of city ids instead of querying by name every time (permanent)
        // TODO: Add call throttler

        Utils.ensureAllQueryParams(query, "lat", "lng");
        String lat = query.get("lat");
        String lng = query.get("lng");

        try (HttpResponse response = getApiRegistry().startCall("weather")
          .buildUrl(url ->
            url.path("weather")
              .query("lat", lat)
              .query("lon", lng))
          .execute(httpHelper)) {
          String content = StringUtils.toString(response.getStream(), "UTF-8");
          return Response.create(200).json(content);
        }
      }))
//      .addMethodRoute(GET, "city", ((method, path, params, query, request, meta, provider) -> {
//        Utils.ensureAllQueryParams(query, "city", "country");
//        String city = query.get("city");
//        String country = query.get("country");
//
//        String countryCode = universalStorage.getCountryCode(country);
//
//        try (HttpResponse response = getApiRegistry().startCall("weather")
//          .buildUrl(url -> url.path("weather")
//            .query("q", city + "," + countryCode))
//          .execute(httpHelper)) {
//          String content = StringUtils.toString(response.getStream(), "UTF-8");
//          return Response.create(200).json(content);
//        }
//      }))
      .exitSubRoute() // weather

      // Events
      .enterSubRoute("events/")
      .addMethodRoute(GET, "location", (method, path, params, query, request, meta, provider) -> {
        Utils.ensureAllQueryParams(query, "lat", "lng");
        String lat = query.get("lat");
        String lng = query.get("lng");

        String geohash = GeoHash.geoHashStringWithCharacterPrecision(Double.valueOf(lat), Double.valueOf(lng), 5);

        try (HttpResponse response = getApiRegistry().startCall("events")
          .buildUrl(url -> url.path("events.join")
            .query("geoPoint", geohash)
            .query("size", "10"))
          .execute(httpHelper)) {
          String content = StringUtils.toString(response.getStream(), "UTF-8");
          return Response.create(200).json(content);
        }
      })
      .exitSubRoute() // events

      // Restaurants
      .enterSubRoute("restaurants/")
      .addMethodRoute(GET, "location", (method, path, params, query, request, meta, provider) -> {
        Utils.ensureAllQueryParams(query, "lat", "lng");
        String lat = query.get("lat");
        String lng = query.get("lng");

        try (HttpResponse response = getApiRegistry().startCall("restaurants")
          .buildUrl(url -> url.path("geocode")
            .query("lat", lat)
            .query("lon", lng)).execute(httpHelper)) {
          String content = StringUtils.toString(response.getStream(), "UTF-8");
          return Response.create(200).json(content);
        }
      })
      .exitSubRoute() // restaurants

      // News
      .enterSubRoute("news/")
//      .addMethodRoute(GET, "top", ((method, path, params, query, request, meta, provider) -> {
//        Utils.ensureAllQueryParams(query, "country");
//        String country = query.get("country");
//        String countryCode = universalStorage.getCountryCode(country);
//
//        try (HttpResponse response = getApiRegistry().startCall("news")
//          .buildUrl(url -> url.path("top-headlines")
//            .query("country", countryCode))
//          .execute(httpHelper)) {
//          String content = StringUtils.toString(response.getStream(), "UTF-8");
//          return Response.create(200).json(content);
//        }
//      }))
      .exitSubRoute() // news

      // Country
      .enterSubRoute("country/")
      .addMethodRoute(GET, "code/(?<code>\\w+)", (method, path, params, query, request, meta, provider) -> {
        Utils.ensureAllQueryParams(params, "code");
        String code = params.get("code");

        try (HttpResponse response = getApiRegistry().startCall("country")
          .buildUrl(url -> url.path("alpha")
            .path(code))
          .execute(httpHelper)) {
          String content = StringUtils.toString(response.getStream(), "UTF-8");
          return Response.create(200).json(content);
        }
      })

      .exitSubRoute() // country
      .exitSubRoute() // source

      .enterSubRoute("aggregate/")
      .addMethodRoute(GET, "all/(?<id>\\d+)", ((method, path, params, query, request, meta, provider) -> {
        int id = Integer.parseInt(params.get("id"));

        String res = aggregateCache.getIfPresent(id);
        if (res != null) {
          return Response.create(200).json(res);
        }

        TravelLocation location = universalStorage.getTravelLocation(id);
        if (location == null)
          throw new HttpException(404, "TravelLocation not found: " + id)
            .body("Invalid id: " + id);

        String city = location.getCity();
        String countryCode = location.getIso2();
        float lat = location.getLatlong()[0];
        float lng = location.getLatlong()[1];
        String latStr = String.valueOf(lat);
        String lngStr = String.valueOf(lng);
        String geohash = GeoHash.geoHashStringWithCharacterPrecision(lat, lng, 5);

        // Guarded-by: Itself and the countdown latch
        Map<String, ApiHttpRequest> requests = new HashMap<>();

//        ApiHttpRequest weatherReq = apiRegistry.startCall("weather",
//          WeatherHourlyExtractor.get());
//        weatherReq.buildUrl().path("forecast")
//          .query("lat", latStr)
//          .query("lon", lngStr)
//          .query("units", "imperial");
        ApiHttpRequest weatherReq = apiRegistry.startCall("weather",
          WeatherHourlyExtractor.get());
        weatherReq.buildUrl().path("forecast")
          .query("q", city + "," + countryCode)
          .query("units", "imperial");
        requests.put("weather", weatherReq);

        ApiHttpRequest eventsReq = getApiRegistry().startCall("events",
          EventsExtractor.get())
          .buildUrl(url -> url.path("events.join")
//            .query("geoPoint", geohash)
            .query("city", city)
            .query("countryCode", countryCode)
            .query("size", "10"));
        requests.put("events", eventsReq);


        ApiHttpRequest restaurantsReq = getApiRegistry().startCall("restaurants",
          RestaurantsExtractor.get())
          .buildUrl(url -> url.path("geocode")
            .query("lat", String.valueOf(lat))
            .query("lon", String.valueOf(lng)));
        requests.put("restaurants", restaurantsReq);

        ApiHttpRequest countryReq = getApiRegistry().startCall("country",
          JsonExtractor.identity());
        countryReq.buildUrl().path("alpha").path(countryCode);
        requests.put("country", countryReq);

        CountDownLatch latch = new CountDownLatch(requests.size());
        ICPWrapper<JsonObject> payload = ICPWrapper.of(new JsonObject());

        ICP.setCompoundPermission(payload, Permissions.getHoldsLockPermission(payload),
          latch.getPermission());

        latch.registerWaiter();

        // Fork requests
        requests.forEach((name, req) -> {
          ICP.setPermission(req, latch.getPermission());
          requestExecutor.execute(Task.ofThreadSafe(() -> {
            try {
              latch.registerCountDowner();
              JsonElement content = req.execute(httpHelper)
                .getExtractedJson().orElseThrow(() -> new IOException("Request content missing"));
              synchronized (payload) {
                payload.target().add(name, content);
              }
            } catch (IOException e) {
              logger.log(Level.SEVERE, e.getMessage(), e);
            } finally {
              latch.countDown();
            }
          }));
        });


        try {
          latch.await();
        } catch (InterruptedException e) {
          throw new HttpException(404, e);
        }

        synchronized (payload) {
          JsonObject json = payload.target();
          String jsonString = UniversalStorage.GSON.toJson(json);
          aggregateCache.put(id, jsonString);
          return Response.create(200)
            .json(jsonString);
        }
      }))
      .exitSubRoute()
      .build(handler);
  }

  private PatternRequestHandler createPageRoutes(RequestHandler handler) {
    return new PatternRequestHandler.Builder("/")
      .addRoute("login", new Route() {
        private final HtmlRoute htmlRoute = new HtmlRoute("login.html");

        {
          ICP.setPermission(this, Permissions.getThreadSafePermission());
        }

        @Override
        public Response.Builder accept(Method method, String path, Map<String, String> params, Map<String, String> query,
                                       Request request, Map<String, Object> meta, ServerProvider provider) throws HttpException, IOException {

          if (method.equals(GET)) {
            if (meta.containsKey("session")) {
              return Response.create(302).header("Location", "/");
            }

            return htmlRoute.accept(method, path, request, meta, provider);
          } else if (method.equals(Method.POST)) {
            DatabaseHelper db = DatabaseHelper.getInstance();
            try {
              Map<String, String> queryParams = request.getFormEncodedBody();
              // Login, create session storage and put username in it,
              // and redirect to main page
              db.login(queryParams.get("username"), queryParams.get("password").toCharArray());
              Map.Entry<String, SessionManager.SessionStorage>
                session = SessionManager.generateDetachedSessionStorage();
              meta.put(SessionManager.META_DETACHED_SESSION, session);
              session.getValue().putItem("user", queryParams.get("username"));
              return Response.create(302).header("Location", "/");
            } catch (Exception e) {
              if (e.getMessage().equalsIgnoreCase("Username or password incorrect"))
                logger.warning("Login failed");
              else
                logger.log(Level.SEVERE, e.getMessage(), e);
              throw new HttpException(401);
            }
          }

          return null;

        }
      })
      .addRoute(".*", new Route() {
        private final HtmlRoute htmlRoute = new HtmlRoute("index.html");

        {
          ICP.setPermission(this, Permissions.getThreadSafePermission());
        }

        @Override
        public Response.Builder accept(Method method, String path, Map<String, String> params, Map<String, String> query,
                                       Request request, Map<String, Object> meta, ServerProvider provider) throws HttpException, IOException {
          if (!meta.containsKey("session")) {
            return Response.create(302).header("Location", "/login");
          }

          return htmlRoute.accept(method, path, request, meta, provider);
        }
      })
      .build(handler);
  }

  @Override
  public ApiRegistry getApiRegistry() {
    return apiRegistry;
  }

  @Override
  public int getPort() {
    return props.getPort();
  }

  @Override
  public String getHost() {
    return props.getHost();
  }

  @Override
  public Settings getSettings() {
    return settings;
  }

  private static CookieManager readCookieManagerFromStorage(Props props) {
    Path path = props.getStorageDirectory().resolve(Constants.STORAGE_CLIENT_COOKIES);
    if (Files.exists(path)) {
      try {
        ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path));
        return (CookieManager) in.readObject();
      } catch (IOException | ClassNotFoundException | ClassCastException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
        throw new RuntimeException("Could not read client cookies data");
      }
    } else {
      return new CookieManager();
    }
  }

  private static void saveCookieManagerToStorage(CookieManager cookieManager, Props props) throws IOException {
    Path path = props.getStorageDirectory().resolve(Constants.STORAGE_CLIENT_COOKIES);
    Files.createDirectories(path.getParent());

    ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(path));
    out.writeObject(cookieManager);
    out.flush();
    out.close();
  }

  public static void main(String[] args) throws Exception {
    Main main = new Main();
    Runtime.getRuntime().addShutdownHook(new Thread(Task.ofThreadSafe(main::stop)));
    main.start();
  }
}
