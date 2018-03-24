// $Id: PrivatePermission.java 11 2017-06-07 19:39:32Z pjh $

package icp.core;

import java.util.logging.Logger;

/**
 * This permission allows get and call, but not put or reset.  It is permanently thread-safe, like
 * all core permissions.
 */
class FrozenPermission implements Permission {
  // for logging debugging info
  private static final Logger logger = Logger.getLogger("icp.core");

  public FrozenPermission() {
  }

  /**
   * Always succeeds.
   */
  public void checkCall(Object target) {
  }

  /**
   * Always succeeds.
   */
  public void checkGet(Object target) {
  }

  /**
   * Always throws an intent error.
   */
  public void checkPut(Object target) {
    throw new IntentError(String.format("task '%s' cannot write object '%s' (frozen)",
      Task.currentTask(), ICP.identityToString(target)));
  }

  /**
   * Always throws an intent error.
   */
  public void checkResetPermission(Object target) {
    throw new IntentError(String.format("task '%s' cannot reset permission of object '%s' (frozen)",
      Task.currentTask(), ICP.identityToString(target)));
  }
}

