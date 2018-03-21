package edu.unh.letsmeet.engine;

import java.io.IOException;
import java.util.Map;

@FunctionalInterface
public interface RequestHandler {

  Response.Builder handleRequest(ServerProvider provider, Request request, Map<String, Object> meta)
    throws HttpException, IOException;
}
