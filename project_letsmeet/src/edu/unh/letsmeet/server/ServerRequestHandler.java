package edu.unh.letsmeet.server;

import edu.unh.letsmeet.engine.Request;
import edu.unh.letsmeet.engine.RequestHandler;
import edu.unh.letsmeet.engine.Response;
import edu.unh.letsmeet.engine.ServerProvider;
import icp.core.ICP;
import icp.core.Permissions;

import java.util.Map;

public class ServerRequestHandler implements RequestHandler {

  public ServerRequestHandler() {
    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

  @Override
  public Response handleRequest(ServerProvider provider, Request request, Map<String, Object> meta) {
    return new Response.Builder(200).body("Hello World").build();
  }
}
