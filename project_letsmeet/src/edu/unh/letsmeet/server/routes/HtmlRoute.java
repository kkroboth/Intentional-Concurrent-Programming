package edu.unh.letsmeet.server.routes;

import edu.unh.letsmeet.Utils;
import edu.unh.letsmeet.engine.HttpException;
import edu.unh.letsmeet.engine.Method;
import edu.unh.letsmeet.engine.Request;
import edu.unh.letsmeet.engine.Response;
import edu.unh.letsmeet.engine.Route;
import edu.unh.letsmeet.engine.ServerProvider;
import edu.unh.letsmeet.server.PagesRequestHandler;
import icp.core.ICP;
import icp.core.Permissions;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HtmlRoute implements Route {
  private static final Logger logger = Logger.getLogger("HtmlRoute");
  private final Path path;

  public HtmlRoute(String relativePath) {
    this.path = Paths.get(relativePath);
    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

  @Override
  public Response accept(Method method, String path, Request request, Map<String, Object> meta,
                         ServerProvider provider) throws HttpException {
    Path root = provider.getSettings().get(PagesRequestHandler.SETTING_PAGES_DIRECTORY);

    try {
      return new Response.Builder(200)
        .html(Utils.readFile(root.resolve(this.path).toAbsolutePath())).build();
    } catch (IOException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      return new Response.Builder(404).build();
    }
  }
}
