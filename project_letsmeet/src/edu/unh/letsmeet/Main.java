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
import icp.lib.ICPExecutors;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.unh.letsmeet.engine.Method.GET;

public class Main {
  private static final Logger logger = Logger.getLogger("Main");

  // Guarded-by: Main instance
  private HttpServer httpServer;
  // Guarded-by: Main instance
  private SessionManager sessions;

  public Main() {
    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

  public synchronized void start() throws IOException {
    Props props = Props.getInstance();

    // Middleware
    sessions = SessionManager.readFromStorage();
    List<Middleware> middlewares = new ArrayList<>();
    middlewares.add(sessions);

    httpServer = new HttpServer(props.getHost(), props.getPort(),
      ICPExecutors.newICPExecutorService(Executors.newCachedThreadPool()),
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
    // Add static directories
    List<Path> dirs = new ArrayList<>();
    dirs.add(props.getStaticDirectory());
    dirs.add(props.getStaticDirectory());
    dirs.add(props.getNodemodulesDirectory());

    settings.set(StaticFilesRequestHandler.SETTING_STATIC_DIRECTORIES, dirs);
    settings.set(StaticFilesRequestHandler.SETTING_URL_PATH, "/static/");

    // Asset directory
    Path value = props.getProjectPath().resolve("assets").toAbsolutePath();
    settings.set("directory.assets", value);

    // start the server!
    httpServer.start();
  }

  public synchronized void stop() {
    try {
      httpServer.stop();
      DatabaseHelper.getInstance().close();
      SessionManager.saveToStorage(sessions);
    } catch (SQLException | IOException e) {
      logger.log(Level.SEVERE, e.toString(), e);
    }
  }

  public ApiRequestHandler createApiHandler() {
    return new ApiRequestHandler.Builder("/api/")
      .addMethodRoute(GET, "map/points", (method, path, request, meta, provider) -> {
        Path pointsPath = ((Path) provider.getSettings().get("directory.assets"))
          .resolve("airports_min.dat");
        try {
          String res = Utils.readFile(pointsPath);
          return new Response.Builder(200)
            .plain(res).build();
        } catch (IOException e) {
          throw new HttpException(500, e);
        }
      })
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

          if (method.equals(GET)) {
            if (meta.containsKey("session")) {
              return new Response.Builder(302).header("Location", "/").build();
            }

            return htmlRoute.accept(method, path, request, meta, provider);
          } else if (method.equals(Method.POST)) {
            DatabaseHelper db = DatabaseHelper.getInstance();
            try {
              Map<String, String> params = request.getFormEncodedBody();
              // Login, create session storage and put username in it,
              // and redirect to main page
              db.login(params.get("username"), params.get("password").toCharArray());
              Map.Entry<String, SessionManager.SessionStorage>
                session = SessionManager.generateDetachedSessionStorage();
              meta.put(SessionManager.META_DETACHED_SESSION, session);
              session.getValue().putItem("user", params.get("username"));
              return new Response.Builder(302).header("Location", "/").build();
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
