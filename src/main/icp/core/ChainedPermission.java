// $Id: PrivatePermission.java 9 2017-06-05 20:23:55Z pjh $

package icp.core;

import java.util.logging.Logger;

import icp.lib.Task;

/**
 * A chained permission simply connects two permissions (both of
 * which might be also a chained permission). For a chained permission
 * to be valid for a given operation, both of its permissions must be true
 * for that operation.
 */
final class ChainedPermission implements Permission
{
  // for logging debugging info
  private static final Logger logger = Logger.getLogger("icp.core");

  // the two permissions 
  private Permission first;
  private Permission second;

  // private constructor
  private ChainedPermission()
  {
  }

  /** Create a permission that chains together two given permissions.
   *
   *  @param first first permission to be chained together.
   *  @param second second permission to be chained together.
   *
   *  @return returns the new chained permission.
   *
   */
  static ChainedPermission newInstance(Permission first,
    Permission second)
  {
    ChainedPermission ret = new ChainedPermission();
    ret.first = first;
    ret.second = second;
    return ret;
  }

  /** Validate permission for calling task to make a call.
   *
   *  @throws IntentError if the access is not allowed.
   *
   */
  @Override
  public void checkCall()
  {
    first.checkCall();
    second.checkCall();
  }

  /** Validate permission for calling task to get a field.
   *
   *  @throws IntentError if the access is not allowed.
   *
   */
  @Override
  public void checkGet()
  {
    first.checkGet();
    second.checkGet();
  }

  /** Validate permission for calling task to put a field.
   *
   *  @throws IntentError if the access is not allowed.
   *
   */
  @Override
  public void checkPut()
  {
    first.checkPut();
    second.checkPut();
  }

  /** Validate permission for calling task to reset the permission.
   *
   *  @throws IntentError if the reset is not allowed.
   *
   */
  @Override
  public void checkResetPermission()
  {
    first.checkResetPermission();
    second.checkResetPermission();
  }
}
