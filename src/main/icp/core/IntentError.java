// $Id$

package icp.core;

/**
 * Exception thrown when a permission violation occurs.
 * <p>
 * <em>Permissions:</em> instances of this class are permanently thread-safe.
 */
final public class IntentError extends RuntimeException {

  private static final long serialVersionUID = -7170077879053936157L;

  public IntentError(String message, Throwable cause) {
    super(message, cause);
  }

  public IntentError(String msg) {
    this(msg, null);
  }
}

