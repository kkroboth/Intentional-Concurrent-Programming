// $Id$

package icp.core;

/**
 * Internal errors.  These errors correspond to assertions and, in a bug free system, should never
 * be thrown.
 */
class ICPInternalError extends AssertionError {

  private static final long serialVersionUID = -8929786099230821215L;

  ICPInternalError(String message, Throwable cause) {
    super(message, cause);
  }

  ICPInternalError(String message) {
    this(message, null);
  }
}
