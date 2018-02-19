package edu.unh.letsmeet;

import edu.unh.letsmeet.command.CommandLineScanner;
import edu.unh.letsmeet.engine.HttpException;
import edu.unh.letsmeet.engine.HttpServer;
import edu.unh.letsmeet.engine.Method;
import edu.unh.letsmeet.engine.Middleware;
import edu.unh.letsmeet.engine.Request;
import edu.unh.letsmeet.engine.Response;
import edu.unh.letsmeet.engine.Route;
import edu.unh.letsmeet.engine.ServerProvider;
import edu.unh.letsmeet.engine.Settings;
import edu.unh.letsmeet.engine.security.SessionManager;
import edu.unh.letsmeet.server.ApiRequestHandler;
import edu.unh.letsmeet.server.PagesRequestHandler;
import edu.unh.letsmeet.server.StaticFilesRequestHandler;
import edu.unh.letsmeet.server.routes.HtmlRoute;
import edu.unh.letsmeet.users.DatabaseHelper;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
  private static final Logger logger = Logger.getLogger("Main");
  private HttpServer httpServer;

  public Main() {
    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

  public synchronized void start() throws IOException {
    Props props = Props.getInstance();

    // Middleware
    SessionManager sessions = new SessionManager();
    List<Middleware> middlewares = new ArrayList<>();
    middlewares.add(sessions);

    httpServer = new HttpServer(props.getHost(), props.getPort(),
      new PagesRequestHandler(
        createPageRoutes(),

        // Static files
        new StaticFilesRequestHandler(
          // Finally the api handler
          createApiHandler())
      ), middlewares);
    DatabaseHelper.getInstance(); // Starts database

    // Add settings
    Settings settings = httpServer.getSettings();
    settings.set(PagesRequestHandler.SETTING_PAGES_DIRECTORY, props.getPagesDirectory());
    settings.set(StaticFilesRequestHandler.SETTING_STATIC_DIRECTORY, props.getStaticDirectory());
    settings.set(StaticFilesRequestHandler.SETTING_URL_PATH, "/static/");

    // start the server!
    httpServer.start();
  }

  public synchronized void stop() {
    try {
      httpServer.stop();
      DatabaseHelper.getInstance().close();
    } catch (SQLException | IOException e) {
      logger.log(Level.SEVERE, e.toString(), e);
    }
  }

  public ApiRequestHandler createApiHandler() {
    return new ApiRequestHandler.Builder("/api/")
      .addRoute("login", (method, path, request, meta, provider) -> {
        if (!method.equals(Method.POST)) throw new HttpException(405);
        meta.put(SessionManager.META_CREATE_SESSION, true);
        return new Response.Builder(200)
          .json("{\"status\": \"good\"}")
          .build();
      }).build();
  }

  public Map<String, Route> createPageRoutes() {
    return new Route.Builder()
      .addRoute("/", new Route() {
        private final HtmlRoute htmlRoute = new HtmlRoute("index.html");

        {
          ICP.setPermission(this, Permissions.getThreadSafePermission());
        }

        @Override
        public Response accept(Method method, String path, Request request,
                               Map<String, Object> meta, ServerProvider provider) throws HttpException {
          if (!meta.containsKey("session")) {
            return new Response.Builder(302).header("Location", "/login").build();
          }

          return htmlRoute.accept(method, path, request, meta, provider);
        }
      })
      .addRoute("/login", new Route() {
        private final HtmlRoute htmlRoute = new HtmlRoute("login.html");

        {
          ICP.setPermission(this, Permissions.getThreadSafePermission());
        }

        @Override
        public Response accept(Method method, String path, Request request,
                               Map<String, Object> meta, ServerProvider provider) throws HttpException {

          if (method.equals(Method.GET)) {
            if (meta.containsKey("session")) {
              return new Response.Builder(302).header("Location", "/").build();
            }

            return htmlRoute.accept(method, path, request, meta, provider);
          } else if (method.equals(Method.POST)) {
            // TODO: Read json and complete this!
          }

          return null;

        }
      })
      .done();
  }

  public static void main(String[] args) throws Exception {
    Main main = new Main();
    Runtime.getRuntime().addShutdownHook(new Thread(Task.ofThreadSafe(main::stop)));
    main.start();


    CommandLineScanner cmdline = new CommandLineScanner();
    cmdline.parseInput();
  }

}
