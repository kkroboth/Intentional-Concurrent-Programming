// $Id$

package icp.core;

import icp.lib.Task;

import javassist.ClassPool;
import javassist.Loader;
import javassist.Translator;
import javassist.NotFoundException;;
import javassist.CannotCompileException;;

import java.util.logging.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Main class for ICP, a Java-based intentional concurrent programming system.
 *
 * Intents are enforced by permissions attached to objects.
 *
 * Javassist is used to edit the user bytecode to insert a permission check
 * prior to each method call, get of a field, or put of a field. The bytecode
 * is edited as the classes are loaded. The bytecode is also edited to
 * potentially insert code after every call to a superclass constructor, in
 * order to generate the initial permission for a new object.
 */
final public class ICP {

  private static final Logger logger = Logger.getLogger("icp.core");

  // private constructor: should not be instantiated
  private ICP() {}

  /**
   * Reset the permission of one object with a same-as permission pointing to
   * another object. (Checks for the same-as permission are forwarded to the
   * permission of the second object.) The existing permission on the first
   * object must allow the permission to be reset.
   *
   * @param follower object to have permission reset.
   * @param leader   object to be pointed to by same-as permission.
   */
  public static void samePermissionAs(Object follower, Object leader)
  {
    PermissionSupport.setPermission(follower, 
      SameAsPermission.newInstance(leader));
  }

  /**
   * Reset the permission of an object with the given permission. The
   * existing permission on the object must allow the permission to be
   * reset.
   *
   * @param target     object to have permission set.
   * @param permission permission to place in the object.
   */
  public static void setPermission(Object target, Permission permission)
  {
    PermissionSupport.setPermission(target, permission);
  }

  // track whether the main thread has been bootstrapped
  private static boolean mainThreadBootstrapped = false;

  /**
   *  Has the main thread been bootstrapped?
   *
   *  @return true if the main thread has been bootstrapped
   */
  public static boolean mainThreadBootstrapped()
  {
    return mainThreadBootstrapped;
  }

  /**
   *  Announce that the main thread has been bootstrapped.
   */
  public static void announceMainThreadBootstrapped()
  {
    mainThreadBootstrapped = true;
  }
}


