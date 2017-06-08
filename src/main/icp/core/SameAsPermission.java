// $Id: PrivatePermission.java 9 2017-06-05 20:23:55Z pjh $

package icp.core;

import java.util.logging.Logger;

import icp.lib.Task;

/**
 * A same-as permission points to an object. The check methods for the same-as
 * permission will be forwarded to the permission of the object pointer to.
 * However, a same-as permission cannot be reset.
 */
final class SameAsPermission implements Permission
{
  // for logging debugging info
  private static final Logger logger = Logger.getLogger("icp.core");

  // the object whose permission will be used
  private final Object obj;

  // private constructor
  private SameAsPermission(Object obj)
  {
    this.obj = obj;
  }

  /** Create a same-as permission.
   *
   *  @param obj the object's whose permission
   *  @param second second permission to be chained together.
   *
   *  @return returns the new chained permission.
   *
   */
  static SameAsPermission newInstance(Object obj)
  {
    return new SameAsPermission(obj);
  }

  /** Validate permission for calling task to make a call.
   *
   *  @throws IntentError if the access is not allowed.
   *
   */
  @Override
  public void checkCall()
  {
    PermissionSupport.getPermission(obj).checkCall();
  }

  /** Validate permission for calling task to get a field.
   *
   *  @throws IntentError if the access is not allowed.
   *
   */
  @Override
  public void checkGet()
  {
    PermissionSupport.getPermission(obj).checkGet();
  }

  /** Validate permission for calling task to put a field.
   *
   *  @throws IntentError if the access is not allowed.
   *
   */
  @Override
  public void checkPut()
  {
    PermissionSupport.getPermission(obj).checkPut();
  }

  /** Validate permission for calling task to reset the permission.
   *
   *  @throws IntentError if the reset is not allowed.
   *
   */
  @Override
  public void checkResetPermission()
  {
    throw new IntentError("cannot reset a same-as permission");
  }
}
