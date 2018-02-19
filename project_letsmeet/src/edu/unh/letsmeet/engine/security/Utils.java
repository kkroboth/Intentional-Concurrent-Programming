package edu.unh.letsmeet.engine.security;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.stream.Stream;

public final class Utils {

  private Utils() {
    // nope
  }

  /**
   * Generates a secure string with a given byte length and secure random instance.
   *
   * @param secureRandom
   * @param byteLength   Length of bytes to generate from secureRandom
   * @return UTF-8 repr of bytes
   */
  public static String generateSecureString(SecureRandom secureRandom, int byteLength) {
    byte[] bytes = new byte[byteLength];
    secureRandom.nextBytes(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  public static String createSetCookie(String key, String value, String path, LocalDateTime expiration,
                                       String... options) {
    ZonedDateTime zonedDateTime = expiration.atZone(ZoneId.of("GMT"));
    String cookieDate = zonedDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    StringBuilder optionBuilder = new StringBuilder();
    Stream.of(options).forEach(option -> optionBuilder.append(option).append("; "));
    return String.format("%s=%s; expires=%s; %s Path=%s",
      key, value, cookieDate, optionBuilder.toString(), path);
  }

  public static String base64Encode(byte[] val) {
    return Base64.getEncoder().encodeToString(val);
  }

  public static String base64Encode(String val) {
    return Base64.getEncoder().encodeToString(val.getBytes(StandardCharsets.UTF_8));
  }

  public static String base64Decode(String val) {
    return new String(Base64.getDecoder().decode(val), StandardCharsets.UTF_8);
  }
}
