package icp.core;

import java.util.logging.Logger;

/**
 * Same as {@link SingleCheckPermission} but for Task-based registration.
 * {@code {@link #singleCheck()}} returns a string for the current
 * tasks' intent error.
 */
public abstract class SingleCheckTaskPermission implements Permission {
  private static final Logger logger = Logger.getLogger("icp.core");

  /**
   * Primary constructor.
   *
   * @param resettable whether the permission can be reset
   */
  protected SingleCheckTaskPermission(boolean resettable) {
    this.resettable = resettable;
  }

  /**
   * Equivalent to {@code SingleCheckPermission(false)}.
   */
  protected SingleCheckTaskPermission() {
    this(false);
  }

  private final boolean resettable;

  /**
   * The call/read/write check.
   *
   * @return intent error iff call/read/write is not allowed. Null if otherwise.
   * An empty string is an intent error, only null means check is valid.
   */
  protected abstract String singleCheck();

  // can calls to currentTask fail here from not being a task?
  private void doCheck(String intentErr, Object target, String message) {
    if (intentErr != null) {
      String cause;
      String trimmed = intentErr.trim();
      cause = trimmed.startsWith("(") ? " " + trimmed : " (" + trimmed + ")";
      throw new IntentError(String.format("task '%s' %s on '%s'%s",
        Task.currentTask(), message, ICP.identityToString(target), cause));
    }
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
    doCheck(resettable ? null : "", target, "cannot reset permission");
  }
}
