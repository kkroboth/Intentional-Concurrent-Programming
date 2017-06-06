// $Id$

package icp.core;

/**
 * Permissions control access to objects.
 *
 */
public interface Permission
{
  /** Can the current task call a method on the object protected by
   *  this permission? If not, an exception is thrown.
   *
   *  @throws IntentError if a call is not allowed.
   */
  public void checkCall();

  /** Can the current task read a field of the object protected by
   *  this permission? If not, an exception is thrown.
   *
   *  @throws IntentError if a get is not allowed.
   */
  public void checkGet();

  /** Can the current task write a field on the object protected by
   *  this permission? If not, an exception is thrown.
   *
   *  @throws IntentError if a put is not allowed.
   */
  public void checkPut();

  /** Can the current task reset the permission of the object protected by
   *  this permission? If not, an exception is thrown.
   *
   *  A default method, which always throws an IntentError, is provided
   *  for checkResetPermission because, other than a private permission,
   *  it will be rare to allow a permission to be reset.
   *
   *  @throws IntentError if a reset is not allowed.
   */
  default void checkResetPermission()
  {
    throw new IntentError("cannot reset permission");
  }
}

