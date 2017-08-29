// $Id$

package icp.core;

/**
 * Permissions, used to control access to objects.
 * <p>
 * Permissions can be obtained from the ICP framework (e.g., from synchronizers) and attached to
 * objects.  Permission methods are called internally by the framework and should not be used
 * directly.
 *
 * @see Permissions
 */
public interface Permission {
  /**
   * Can the current task call a method on the object protected by
   * this permission? If not, an exception is thrown.
   *
   * @throws IntentError if a call is not allowed.
   */
  void checkCall(Object target);

  /**
   * Can the current task read a field of the object protected by
   * this permission? If not, an exception is thrown.
   *
   * @throws IntentError if a get is not allowed.
   */
  void checkGet(Object target);

  /**
   * Can the current task write a field on the object protected by
   * this permission? If not, an exception is thrown.
   *
   * @throws IntentError if a put is not allowed.
   */
  void checkPut(Object target);

  /**
   * Can the current task reset the permission of the object protected by
   * this permission? If not, an exception is thrown.
   *
   * @throws IntentError if a reset is not allowed.
   */
  void checkResetPermission(Object target);
}

