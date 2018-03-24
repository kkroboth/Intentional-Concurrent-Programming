// $Id: PrivatePermission.java 9 2017-06-05 20:23:55Z pjh $

package icp.core;

import java.util.logging.Logger;

/**
 * A same-as permission points to an object. The check methods for the same-as
 * permission will be forwarded to the permission of the object pointer to.
 * However, a same-as permission cannot be reset.
 */
final class SameAsPermission implements Permission {
  // for logging debugging info
  private static final Logger logger = Logger.getLogger("icp.core");

  // the object whose permission will be used
  private final Object obj;

  // private constructor
  public SameAsPermission(Object obj) {
    this.obj = obj;
  }

  /**
   * Validate permission for calling task to make a call.
   *
   * @throws IntentError if the access is not allowed.
   */
  public void checkCall(Object target) {
    PermissionSupport.getPermission(obj).checkCall(target);
  }

  /**
   * Validate permission for calling task to get a field.
   *
   * @throws IntentError if the access is not allowed.
   */
  public void checkGet(Object target) {
    PermissionSupport.getPermission(obj).checkGet(target);
  }

  /**
   * Validate permission for calling task to put a field.
   *
   * @throws IntentError if the access is not allowed.
   */
  public void checkPut(Object target) {
    PermissionSupport.getPermission(obj).checkPut(target);
  }

  /**
   * Validate permission for calling task to reset the permission.
   *
   * @throws IntentError if the reset is not allowed.
   */
  public void checkResetPermission(Object target) {
    throw new IntentError(String.format("cannot reset a same-as permission on '%s'", ICP.identityToString(target)));

  }
}
