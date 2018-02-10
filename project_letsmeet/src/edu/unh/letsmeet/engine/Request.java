package edu.unh.letsmeet.engine;

import edu.unh.letsmeet.Props;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * Http request.
 */
public class Request {
  private static final Logger logger = Logger.getLogger("HTTP-Request");
  private static final long MAX_CONTENT_LENGTH = Props.getInstance().getMaxContentLength();

  private final Method method;
  private final Map<String, String> headers;
  private final URI uri;
  private final StringBuilder body;


  private Request(Builder builder) {
    this.method = builder.method;
    this.headers = builder.headers;
    this.uri = builder.uri;
    this.body = builder.body;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(method.toString()).append(" ").append(uri).append('\n');
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      builder.append(entry.getKey()).append(' ').append(entry.getValue()).append('\n');
    }

    if (body != null) {
      int len = Math.min(100, body.length());
      builder.append(body.substring(0, len)).append("\n\n");
    }

    return builder.toString();
  }

  public static Request parse(InputStream stream) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    Builder builder = new Builder();

    // Request line
    String[] requestLine = reader.readLine().split(" ");
    logger.log(FINER, "Request-Line: {0}", Arrays.toString(requestLine));
    if (requestLine.length != 3) {
      logger.warning("Malformed request line");
      throw new IOException("Invalid Request");
    }

    try {
      builder.setMethod(Method.valueOf(requestLine[0].toUpperCase()));
    } catch (IllegalArgumentException e) {
      logger.log(WARNING, "Unsupported method: {0}", requestLine[0].toUpperCase());
      throw new IOException("Invalid Request");
    }

    try {
      builder.setUri(new URI(requestLine[1]));
    } catch (URISyntaxException e) {
      logger.log(WARNING, "Invalid Request-URI: {0}", requestLine[1]);
      throw new IOException(e);
    }

    // Either EOF or contains headers
    String line;
    while (!(line = reader.readLine()).isEmpty()) {
      // parse header
      String[] pair = line.split(":", 2);
      if (pair.length != 2) {
        logger.log(FINE, "Header not in pair format: {0}", line);
        continue;
      }

      builder.addHeader(pair[0], pair[1]);
    }

    // Body
    if (builder.getHeader("Content-Length") == null) {
      return builder.build();
    }

    try {
      long contentLength = Long.parseLong(builder.getHeader("Content-Length"));
      if (contentLength > MAX_CONTENT_LENGTH) {
        logger.log(INFO, "Body length exceeds max content length: {0}", contentLength);
        throw new IOException("Invalid Request");
      }


      StringBuilder sb = new StringBuilder();
      char[] buffer = new char[4096];
      int rem = (int) contentLength; // Assuming never going to read > 2^32 bytes

      while (rem > 0) {
        int read = reader.read(buffer, 0, Math.min(4096, rem));
        if (read <= 0) {
          logger.log(INFO, "Abrupt EOF while reading body");
          throw new IOException("Invalid Request");
        }

        sb.append(buffer, 0, read);
        rem -= read;
      }

      builder.setBody(sb);
    } catch (NumberFormatException e) {
      logger.log(WARNING, "Content-Length invalid format: {0}", builder.getHeader("Content-Length"));
      throw new IOException("Invalid Request");
    }


    return builder.build();
  }

  /**
   * Builder of Request class
   */
  private static class Builder {
    Method method;
    Map<String, String> headers;
    URI uri;
    StringBuilder body;

    Builder() {
      headers = new HashMap<>();
    }

    void setMethod(Method method) {
      this.method = method;
    }

    void addHeader(String name, String value) {
      headers.put(name.toUpperCase(), value.trim());
    }

    String getHeader(String name) {
      return headers.get(name.toUpperCase());
    }

    void setUri(URI uri) {
      this.uri = uri;
    }

    void setBody(StringBuilder body) {
      this.body = body;
    }

    Request build() {
      return new Request(this);
    }
  }

}
