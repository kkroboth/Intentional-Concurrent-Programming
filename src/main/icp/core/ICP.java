// $Id$

package icp.core;

import java.util.logging.Logger;

/**
 * Main class for ICP, a Java-based intentional concurrent programming system.
 * <p>
 * Intents are enforced by permissions attached to objects.
 * <p>
 * Javassist is used to edit the user bytecode to insert a permission check
 * prior to each method call, get of a field, or put of a field. The bytecode
 * is edited as the classes are loaded. The bytecode is also edited to
 * potentially insert code after every call to a superclass constructor, in
 * order to generate the initial permission for a new object.
 * <p>
 * This class cannot be instantiated.
 */
final public class ICP {

  private static final Logger logger = Logger.getLogger("icp.core");

  private ICP() {
    throw new AssertionError("this class cannot be instantiated");
  }

  /**
   * Reset the permission of one object with a same-as permission pointing to
   * another object. (Checks for the same-as permission are forwarded to the
   * permission of the second object.) The existing permission on the first
   * object must allow the permission to be reset.
   * <p>
   * This is equivalent to:
   * {@code setPermission(follower, Permissions.getSamePermissionAs(leader)}
   *
   * @param follower object to have permission reset.
   * @param leader   object to be pointed to by same-as permission.
   */
  public static void samePermissionAs(Object follower, Object leader) {
    PermissionSupport.setPermission(follower,
      Permissions.getSamePermissionAs(leader));
  }

  /**
   * Reset the permission of an object with the given permission. The
   * existing permission on the object must allow the permission to be
   * reset.
   *
   * @param target     object to have permission set.
   * @param permission permission to place in the object.
   */
  public static void setPermission(Object target, Permission permission) {
    PermissionSupport.setPermission(target, permission);
  }

  /**
   * Reset the of an object with multiple permissions that are each checked in the order
   * given to arguments.
   *
   * @param target      object to have permission set.
   * @param permissions Array of permissions for an object
   */
  public static void setCompoundPermission(Object target, Permission... permissions) {
    setPermission(target, Permissions.getCompoundPermission(permissions));
  }

  /**
   * When logging or raising IntentErrors and calling toString on a target, infinite recursion
   * may and will happen when the overrided toString() asserts the permission.
   */
  public static String identityToString(Object target) {
    return target.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(target));
  }
}