package edu.unh.letsmeet.server;

import edu.unh.letsmeet.Utils;
import edu.unh.letsmeet.engine.Request;
import edu.unh.letsmeet.engine.RequestHandler;
import edu.unh.letsmeet.engine.Response;
import icp.core.ICP;
import icp.core.Permissions;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PagesRequestHandler extends RequestHandlerDecorator {
  private static final Logger logger = Logger.getLogger("PagesRequestHandler");

  private final Path pagesPath;
  private final String[][] urls;

  public PagesRequestHandler(Path pagesDirectory, String[][] urls, RequestHandler delegate) {
    super(delegate);
    this.pagesPath = pagesDirectory;
    this.urls = urls;

    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  public PagesRequestHandler(Path pagesDirectory, String[][] urls) {
    this(pagesDirectory, urls, null);
  }

  @Override
  public Response handleRequest(Request request) {
    URI uri = request.getUri();
    String path = uri.getPath();
    for (String[] url : urls) {
      if (url[0].equalsIgnoreCase(path)) {
        try {
          return new Response.Builder(200)
            .html(Utils.readFile(pagesPath.resolve(url[1]).toAbsolutePath())).build();
        } catch (IOException e) {
          logger.log(Level.SEVERE, e.getMessage(), e);
          return new Response.Builder(404).build();
        }
      }
    }

    return super.handleRequest(request);
  }

  public static String[][] buildUrls(String... paths) {
    if (paths.length % 2 != 0) throw new IllegalArgumentException("Paths must be pairs of strings elements");
    String[][] pathArray = new String[paths.length / 2][2];
    for (int i = 0; i < paths.length; i += 2) {
      pathArray[i / 2][0] = paths[i];
      pathArray[i / 2][1] = paths[i + 1];
    }

    return pathArray;
  }
}
