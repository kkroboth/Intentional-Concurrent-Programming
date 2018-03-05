package edu.unh.letsmeet.engine;

import edu.unh.letsmeet.engine.function.CheckedConsumer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.unh.letsmeet.engine.HttpServer.CRLF;

public class Response {
  private final int status;
  private final boolean close;
  private final Map<String, String> headers;
  private final byte[] body;
  private final CheckedConsumer<OutputStream> streamer;
  private final String[] cookies;

  public void createResponse(OutputStream outputStream) throws IOException {
    // Use stringbuilder for header only

    StringBuilder builder = new StringBuilder();
    // Status line
    builder.append("HTTP/1.1 ").append(status).append(" ").append(Status.reasonPhrase(status));
    builder.append(CRLF);

    // Headers
    Map<String, String> headers = new HashMap<>();
    if (close) headers.put("Connection", "close");
    if (body != null) headers.put("Content-Length", String.valueOf(body.length));
    headers.putAll(this.headers); // Allow this.headers to override above

    for (Map.Entry<String, String> header : headers.entrySet()) {
      builder.append(header.getKey()).append(": ").append(header.getValue());
      builder.append(CRLF);
    }

    // Add cookies
    for (String cookie : cookies) {
      builder.append("Set-Cookie").append(": ").append(cookie);
      builder.append(CRLF);
    }

    builder.append(CRLF);
    // Write header
    outputStream.write(builder.toString().getBytes(StandardCharsets.UTF_8));

    // Write body
    if (useStream()) {
      try {
        streamer.accept(outputStream);
      } catch (Exception e) {
        if (e instanceof IOException) throw ((IOException) e);
        throw new RuntimeException(e);
      }
    } else if (body != null) {
      outputStream.write(body);
    }

    outputStream.flush();
  }

  private Response(Builder builder) {
    this.status = builder.status;
    this.close = builder.close;
    this.headers = builder.headers;
    this.body = builder.body;
    this.streamer = builder.streamer;
    this.cookies = builder.cookies.toArray(new String[builder.cookies.size()]);
  }

  private boolean useStream() {
    return streamer != null;
  }

  public static class Builder {
    int status;
    boolean close = true;
    Map<String, String> headers;
    byte[] body;
    CheckedConsumer<OutputStream> streamer;
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
      this.streamer = response.streamer;
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

    public Builder body(byte[] body) {
      this.body = body;
      return this;
    }

    public Builder plain(String body) {
      this.body = body.getBytes(StandardCharsets.UTF_8);
      headers.put("Content-Type", "text/plain; charset=utf8");
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

    public Builder streamFixedLength(CheckedConsumer<OutputStream> streamer, int length) {
      this.streamer = streamer;
      headers.put("Content-Length", String.valueOf(length));
      return this;
    }

    public Builder streamChunked(CheckedConsumer<OutputStream> streamer) {
      this.streamer = outputStream -> streamer.accept(new ChunkedOutputStream(outputStream));
      headers.put("Transfer-Encoding", "chunked");
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
      if (streamer != null && body != null)
        throw new IllegalStateException("Both body and streamer function cannot be set at same time");
      if (status == -1) throw new IllegalStateException("Status code not set");
      return new Response(this);
    }
  }
}
