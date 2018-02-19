package edu.unh.letsmeet.engine;

import java.util.Map;

@FunctionalInterface
public interface RequestHandler {

  Response handleRequest(ServerProvider provider, Request request, Map<String, Object> meta) throws HttpException;
}
