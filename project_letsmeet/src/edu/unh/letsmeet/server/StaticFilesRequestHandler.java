package edu.unh.letsmeet.server;

import edu.unh.letsmeet.Utils;
import edu.unh.letsmeet.engine.ContentTypes;
import edu.unh.letsmeet.engine.Request;
import edu.unh.letsmeet.engine.RequestHandler;
import edu.unh.letsmeet.engine.Response;
import icp.core.ICP;
import icp.core.Permissions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StaticFilesRequestHandler extends RequestHandlerDecorator {
  private static final Logger logger = Logger.getLogger("StaticFilesRequestHandler");

  private final Path staticPath;
  private final String urlPath;

  public StaticFilesRequestHandler(Path staticPath, String urlPath, RequestHandler delegate) {
    super(delegate);
    this.staticPath = staticPath;
    this.urlPath = urlPath;

    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  public StaticFilesRequestHandler(Path staticPath, String urlPath) {
    this(staticPath, urlPath, null);
  }

  @Override
  public Response handleRequest(Request request) {
    String path = request.getUri().getPath();

    if (path.startsWith(urlPath)) {
      String filePath = path.substring(path.indexOf(urlPath, 0) + urlPath.length());
      logger.finer("Finding: " + filePath);

      Path staticFile = staticPath.resolve(filePath).toAbsolutePath();
      logger.finer("Path: " + staticFile);
      if (Files.exists(staticFile)) {
        try {
          return new Response.Builder(200).body(Utils.readFile(staticFile))
            .contentType(ContentTypes.getContentType(Utils.getFileExtension(path)))
            .build();
        } catch (IOException e) {
          logger.log(Level.SEVERE, e.getMessage(), e);
          return new Response.Builder(404).build();
        }
      }
    }

    return super.handleRequest(request);
  }
}
