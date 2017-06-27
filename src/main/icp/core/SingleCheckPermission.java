// $Id$

package icp.core;

import java.util.logging.Logger;

/**
 * A base class for permissions that treat call, read and write homogeneously.
 * Permissions can be created as resettable or non-resettable, and their error message can be
 * customized.
 */
public abstract class SingleCheckPermission implements Permission {

  private static final Logger logger = Logger.getLogger("icp.core");

  /**
   * Primary constructor.
   *
   * @param resettable whether the permission can be reset
   * @param cause      a string used in error messages; {@code null} means no message.
   */
  protected SingleCheckPermission(boolean resettable, String cause) {
    this.resettable = resettable;
    if (cause == null) {
      this.cause = "";
    } else {
      String trimmed = cause.trim();
      this.cause = trimmed.startsWith("(") ? " " + trimmed : " (" + trimmed + ")";
    }
  }

  /**
   * Equivalent to {@code SingleCheckPermission(false, cause)}.
   */
  protected SingleCheckPermission(String cause) {
    this(false, cause);
  }

  /**
   * Equivalent to {@code SingleCheckPermission(resettable, null)}.
   */
  protected SingleCheckPermission(boolean resettable) {
    this(resettable, null);
  }

  /**
   * Equivalent to {@code SingleCheckPermission(false, null)}.
   */
  protected SingleCheckPermission() {
    this(false, null);
  }

  private final String cause;
  private final boolean resettable;

  /**
   * The call/read/write check.
   *
   * @return true iff call/read/write is allowed
   */
  protected abstract boolean singleCheck();

  // can calls to currentTask fail here from not being a task?
  private void doCheck(boolean checked, Object target, String message) {
    if (!checked)
      throw new IntentError(String.format("task '%s' %s on '%s'%s",
          Task.currentTask(), message, target, cause));

  }

  public final void checkCall(Object target) {
    logger.fine(String.format("task '%s' checking call permission on '%s'",
        Task.currentTask(), this));

    doCheck(singleCheck(), target, "cannot call methods");
  }

  public final void checkGet(Object target) {
    logger.fine(String.format("task '%s' checking read permission on '%s'",
        Task.currentTask(), this));
    doCheck(singleCheck(), target, "cannot read fields");
  }

  public final void checkPut(Object target) {
    logger.fine(String.format("task '%s' checking write permission on '%s'",
        Task.currentTask(), this));
    doCheck(singleCheck(), target, "cannot write fields");
  }

  public void checkResetPermission(Object target) {
    logger.fine(String.format("task '%s' checking reset permission on '%s'",
        Task.currentTask(), this));
    doCheck(resettable && singleCheck(), target, "cannot reset permission");
  }
}
