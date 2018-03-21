package edu.unh.letsmeet.engine;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;
import edu.unh.letsmeet.Props;
import edu.unh.letsmeet.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
  private final ListMultimap<String, String> headers;
  private final URI uri;
  private final byte[] body;
  private final Map<String, Cookie> cookies;


  private Request(Builder builder) {
    this.method = builder.method;
    this.headers = builder.headers;
    this.uri = builder.uri;
    this.body = builder.body;
    this.cookies = builder.cookies;
  }

  public Method getMethod() {
    return this.method;
  }

  public URI getUri() {
    return this.uri;
  }

  public String getHeader(String name) {
    List<String> values = this.headers.get(name);
    if (values.isEmpty()) return null;
    return values.get(0);
  }

  public List<String> getHeaders(String name) {
    return this.headers.get(name);
  }

  public byte[] getBody() {
    return this.body;
  }

  public String getStringBody() {
    return new String(this.body, StandardCharsets.UTF_8);
  }

  public Map<String, String> getFormEncodedBody() throws IOException {
    return Utils.parseQueryString(getStringBody());
  }

  public <V> V getJson(Class<V> gsonClass) {
    return new Gson().fromJson(new StringReader(getStringBody()), gsonClass);
  }

  public Cookie getCookie(String key) {
    return cookies.get(key);
  }

  @Override
  public String toString() {
    return method.toString() + " " + uri;
  }

  public static Request parse(InputStream stream) throws HttpException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    Builder builder = new Builder();

    try {
      // Request line
      String[] requestLine = reader.readLine().split(" ");
      logger.log(FINER, "Request-Line: {0}", Arrays.toString(requestLine));
      if (requestLine.length != 3) {
        logger.warning("Malformed request line");
        throw new HttpException(400, "Invalid Request");
      }

      try {
        builder.setMethod(Method.valueOf(requestLine[0].toUpperCase()));
      } catch (IllegalArgumentException e) {
        logger.log(WARNING, "Unsupported method: {0}", requestLine[0].toUpperCase());
        throw new HttpException(405, "Invalid Request");
      }

      try {
        builder.setUri(new URI(requestLine[1]));
      } catch (URISyntaxException e) {
        logger.log(WARNING, "Invalid Request-URI: {0}", requestLine[1]);
        throw new HttpException(400, e);
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
          throw new HttpException(413, "Invalid Request");
        }


        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[4096];
        int rem = (int) contentLength; // Assuming never going to read > 2^32 bytes

        while (rem > 0) {
          int read = reader.read(buffer, 0, Math.min(4096, rem));
          if (read <= 0) {
            logger.log(INFO, "Abrupt EOF while reading body");
            throw new HttpException(400, "Invalid Request");
          }

          sb.append(buffer, 0, read);
          rem -= read;
        }

        builder.setBody(sb);
      } catch (NumberFormatException e) {
        logger.log(WARNING, "Content-Length invalid format: {0}", builder.getHeader("Content-Length"));
        throw new HttpException(400, "Invalid Request");
      }
    } catch (IOException e) {
      throw new HttpException(400, e);
    }


    return builder.build();
  }

  /**
   * Builder of Request class
   */
  private static class Builder {
    Method method;
    ListMultimap<String, String> headers;
    URI uri;
    byte[] body;
    Map<String, Cookie> cookies;

    Builder() {
      cookies = new HashMap<>();
      headers = ArrayListMultimap.create();
    }

    Builder setMethod(Method method) {
      this.method = method;
      return this;
    }

    Builder addHeader(String name, String value) {
      headers.put(name.toUpperCase(), value.trim());
      return this;
    }

    String getHeader(String name) {
      List<String> values = headers.get(name.toUpperCase());
      if (values.isEmpty()) return null;
      return values.get(0);
    }

    Builder setUri(URI uri) {
      this.uri = uri;
      return this;
    }

    Builder setBody(StringBuilder body) {
      this.body = body.toString().getBytes(StandardCharsets.UTF_8);
      return this;
    }

    Request build() {
      // Create cookies from header
      String header = getHeader("Cookie");
      if (header != null) {
        String[] cookies = header.split(";");
        for (String cookie : cookies) {
          cookie = cookie.trim();
          String[] parts = cookie.split("=");
          this.cookies.put(parts[0].trim(), new Cookie(parts[0].trim(),
            parts[1].trim()));
        }
      }

      return new Request(this);
    }
  }

}
