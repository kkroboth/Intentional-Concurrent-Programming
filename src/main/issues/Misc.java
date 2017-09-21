package issues;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilities for issues.
 */
public final class Misc {

  /**
   * Print all messages of all levels to console.
   */
  public static void setupConsoleLogger() {
    ConsoleHandler handler = new ConsoleHandler();
    handler.setLevel(Level.ALL);
    Logger.getLogger("icp.core").addHandler(handler);
    Logger.getLogger("icp.core").setLevel(Level.ALL);
  }
}
