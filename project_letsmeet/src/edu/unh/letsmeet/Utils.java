package edu.unh.letsmeet;

import edu.unh.letsmeet.engine.HttpException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Utils {
  private Utils() {
    // nope
  }

  public static String readFile(Path path) throws IOException {
    return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
  }

  public static byte[] readFileBytes(Path path) throws IOException {
    return Files.readAllBytes(path);
  }

  public static String getFileExtension(String path) {
    return path.substring(path.lastIndexOf(".") + 1);
  }

  public static Map<String, String> parseQueryString(String query) throws IOException {
    Map<String, String> params = new HashMap<>();
    String[] pairs = query.split("&");
    for (String pair : pairs) {
      if (!pair.contains("=")) {
        params.put(pair, "true");
        continue;
      }

      String[] fields = pair.split("=");
      if (fields.length != 2) throw new IOException("Invalid form encoded format");
      params.put(fields[0], fields[1]);
    }

    return Collections.unmodifiableMap(params);
  }

  public static void ensureAllQueryParams(Map<String, String> queryParams, String... required) throws HttpException {
    for (String name : required) {
      if (!queryParams.containsKey(name)) throw new HttpException(400, "Missing query parameter: " + name)
        .body("Required query parameters: " + String.join(",", required));
    }
  }

}
