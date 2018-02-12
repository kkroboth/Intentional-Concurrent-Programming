package edu.unh.letsmeet.server;

import edu.unh.letsmeet.engine.Request;
import edu.unh.letsmeet.engine.RequestHandler;
import edu.unh.letsmeet.engine.Response;
import icp.core.ICP;
import icp.core.Permissions;

public class ServerRequestHandler implements RequestHandler {

  public ServerRequestHandler() {
    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

  @Override
  public Response handleRequest(Request request) {
    return new Response.Builder(200).body("Hello World").build();
  }
}
