package edu.unh.letsmeet.server;

import edu.unh.letsmeet.Utils;
import edu.unh.letsmeet.engine.ContentTypes;
import edu.unh.letsmeet.engine.HttpException;
import edu.unh.letsmeet.engine.Request;
import edu.unh.letsmeet.engine.RequestHandler;
import edu.unh.letsmeet.engine.Response;
import edu.unh.letsmeet.engine.ServerProvider;
import icp.core.ICP;
import icp.core.Permissions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StaticFilesRequestHandler extends RequestHandlerDecorator {
  private static final Logger logger = Logger.getLogger("StaticFilesRequestHandler");

  public static final String SETTING_STATIC_DIRECTORIES = "directories.staticfilesrequesthandler";
  public static final String SETTING_URL_PATH = "urlpath.staticfilesrequesthandler";


  public StaticFilesRequestHandler(RequestHandler delegate) {
    super(delegate);
    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  public StaticFilesRequestHandler() {
    this(null);
  }

  @Override
  public Response handleRequest(ServerProvider provider, Request request, Map<String, Object> meta) throws HttpException, IOException {
    String path = request.getUri().getPath();
    String urlPath = provider.getSettings().get(SETTING_URL_PATH);
    List<Path> staticPaths = provider.getSettings().get(SETTING_STATIC_DIRECTORIES);

    if (path.startsWith(urlPath)) {
      String filePath = path.substring(path.indexOf(urlPath, 0) + urlPath.length());
      logger.finer("Finding: " + filePath);

      for (Path staticPath : staticPaths) {
        Path staticFile = staticPath.resolve(filePath).toAbsolutePath();
        logger.finer("Path: " + staticFile);
        if (Files.exists(staticFile)) {
          try {
            String contentTypeStr;
            ContentTypes.ContentType contentType = ContentTypes.getContentType(Utils.getFileExtension(path));
            if (contentType == null) {
              logger.log(Level.WARNING, "ContentType not registered: " +
                Utils.getFileExtension(path));
              contentTypeStr = "application/octet-stream";
            } else {
              contentTypeStr = contentType.getType();
            }
            return Response.create(200).body(Utils.readFileBytes(staticFile))
              .contentType(contentTypeStr)
              .build();
          } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return Response.create(404).build();
          }
        }
      }
    }

    return super.handleRequest(provider, request, meta);
  }
}
