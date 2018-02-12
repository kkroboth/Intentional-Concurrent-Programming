package edu.unh.letsmeet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Utils {
  private Utils() {
    // nope
  }

  public static String readFile(Path path) throws IOException {
    return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
  }

  public static String getFileExtension(String path) {
    return path.substring(path.lastIndexOf(".") + 1);
  }

}
