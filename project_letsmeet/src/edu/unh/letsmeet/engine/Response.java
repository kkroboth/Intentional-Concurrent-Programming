package edu.unh.letsmeet.engine;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Response {
  private final int status;
  private final boolean close;
  private final Map<String, String> headers;
  private final byte[] body;
  private final String[] cookies;

  public String createResponse() {
    StringBuilder builder = new StringBuilder();
    // Status line
    builder.append("HTTP/1.1 ").append(status).append(" ").append(Status.reasonPhrase(status));
    builder.append("\r\n");

    // Headers
    Map<String, String> headers = new HashMap<>();
    if (close) headers.put("Connection", "close");
    if (body != null) headers.put("Content-Length", String.valueOf(body.length));
    headers.putAll(this.headers); // Allow this.headers to override above

    for (Map.Entry<String, String> header : headers.entrySet()) {
      builder.append(header.getKey()).append(": ").append(header.getValue());
      builder.append("\r\n");
    }

    // Add cookies
    for (String cookie : cookies) {
      builder.append("Set-Cookie").append(": ").append(cookie);
      builder.append("\r\n");
    }

    builder.append("\r\n");
    if (body != null) {
      builder.append(new String(body, StandardCharsets.UTF_8));
    }

    return builder.toString();
  }

  private Response(Builder builder) {
    this.status = builder.status;
    this.close = builder.close;
    this.headers = builder.headers;
    this.body = builder.body;
    this.cookies = builder.cookies.toArray(new String[builder.cookies.size()]);
  }

  public static class Builder {
    int status;
    boolean close = true;
    Map<String, String> headers;
    byte[] body;
    List<String> cookies;

    public Builder(int status) {
      this.status = status;
      headers = new HashMap<>();
      cookies = new ArrayList<>();
    }

    public Builder(Response response) {
      this.status = response.status;
      this.close = response.close;
      this.headers = response.headers;
      this.body = response.body;
      //noinspection unchecked
      this.cookies = new ArrayList(Arrays.asList(response.cookies));
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
      this.body = body.getBytes(StandardCharsets.UTF_8);
      return this;
    }

    public Builder html(String body) {
      this.body = body.getBytes(StandardCharsets.UTF_8);
      headers.put("Content-Type", "text/html; charset=utf8");
      return this;
    }

    public Builder json(String json) {
      this.body = json.getBytes(StandardCharsets.UTF_8);
      headers.put("Content-Type", "application/json; charset=utf8");
      return this;
    }

    public Builder contentType(String type) {
      this.headers.put("Content-Type", type);
      return this;
    }

    public Builder addCookie(String cookie) {
      this.cookies.add(cookie);
      return this;
    }

    public Builder removeCookie(String name, String path) {
      String cookie = String.format("%s=; Max-age=0; path=%s; expires=Thu, 01 Jan 1970 00:00:00 GMT;",
        name, path);
      return addCookie(cookie);
    }

    public Response build() {
      if (status == -1) throw new IllegalStateException("Status code not set");
      return new Response(this);
    }
  }
}
