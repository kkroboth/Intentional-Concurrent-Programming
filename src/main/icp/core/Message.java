// $Id$

package icp.core;

/**
 * Used to terminate and print a message when the system has an error.
 *
 */
final class Message {

  /**
   * Used to terminate the program.
   *
   */
  public static class FatalException extends RuntimeException {
    static final long serialVersionUID = 1;
    public FatalException () {
      super("fatal exception");
    }
  }

  // should not be instantiated
  private Message() {}

  /** Display message to stderr for an internal error and terminate the
   *  program.
   *
   *  @param format message format
   *  @param args   arguments to be plugged into the message format
   *
   *  @throws FatalException always
   */
  public static void fatal(String format, Object... args)
  {
    System.err.printf("[fatal internal error] " + format + "%n", args);
    throw new FatalException();
  }

  /** Display message to stderr for a user error and terminate the
   *  program.
   *
   *  @param format message format
   *  @param args   arguments to be plugged into the message format
   *
   *  @throws FatalException always
   */
  public static void error(String format, Object... args)
  {
    System.err.printf("[user error] " + format + "%n", args);
    throw new FatalException();
  }

  /** Display warning message to stderr for a user error. Does not
   *  terminate the program.
   *
   *  @param format message format
   *  @param args   arguments to be plugged into the message format
   *
   */
  public static void warning(String format, Object... args)
  {
    System.err.printf("[user error] " + format + "%n", args);
  }
}

