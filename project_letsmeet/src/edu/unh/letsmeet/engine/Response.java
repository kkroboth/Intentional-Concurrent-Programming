package edu.unh.letsmeet.engine;

import java.util.HashMap;
import java.util.Map;

public class Response {
  private final int status;
  private final boolean close;
  private final Map<String, String> headers;
  private final String body;


  public String createResponse() {
    StringBuilder builder = new StringBuilder();
    // Status line
    builder.append("HTTP/1.1 ").append(status).append(" ").append(Status.reasonPhrase(status));
    builder.append("\r\n");

    // Headers
    Map<String, String> headers = new HashMap<>();
    if (close) headers.put("Connection", "close");
    if (body != null) headers.put("Content-Length", String.valueOf(body.length()));
    headers.putAll(this.headers); // Allow this.headers to override above

    for (Map.Entry<String, String> header : headers.entrySet()) {
      builder.append(header.getKey()).append(": ").append(header.getValue());
      builder.append("\r\n");
    }

    builder.append("\r\n");
    if (body != null) {
      builder.append(body);
    }

    System.out.println(builder.toString());
    return builder.toString();
  }

  private Response(Builder builder) {
    this.status = builder.status;
    this.close = builder.close;
    this.headers = builder.headers;
    this.body = builder.body;
  }

  public static class Builder {
    int status;
    boolean close = true;
    Map<String, String> headers;
    String body;

    public Builder(int status) {
      this.status = status;
      headers = new HashMap<>();
    }

    public Builder() {
      this(-1);
    }

    public Builder close(boolean close) {
      this.close = close;
      return this;
    }

    public Builder header(String name, String value) {
      this.headers.put(name, value);
      return this;
    }

    public Builder body(String body) {
      this.body = body;
      return this;
    }

    public Builder html(String body) {
      this.body = body;
      headers.put("Content-Type", "text/html; charset=utf8");
      return this;
    }

    public Builder contentType(String type) {
      this.headers.put("Content-Type", type);
      return this;
    }

    public Response build() {
      if (status == -1) throw new IllegalStateException("Status code not set");
      return new Response(this);
    }
  }
}
