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

  /** Initialize ICP: create a class loader that will edit
   *  user bytecode; establish the initial Task for the main thread.
   *
   @  @param className name of user class to be executed
   *  @param args arguments to be passed to the user's main method
   *
   *  @throws java.lang.Throwable propagates on any user exception
   */
  public static void initialize(String className, String[] args)
    throws Throwable
  {
    // need to add the class name to the args before passing them to the
    // bootstrap main method
    String[] newArgs = new String[args.length+1];
    newArgs[0] = className;
    for (int i = 0; i < args.length; i++)
    {
      newArgs[i+1] = args[i];
    }

    Translator t = new BytecodeTranslator();
    ClassPool pool = ClassPool.getDefault();
    Loader cl = new Loader();
    try {
      cl.addTranslator(pool, t);
    } catch (NotFoundException | CannotCompileException e) {
      Message.fatal("internal error in icp.core.Main (addTranslator call):" +e);
    }
    logger.fine("ICP initialized");
    try {
      cl.run("icp.core.ICP$BootStrap", newArgs);
    } catch (ClassNotFoundException | NoSuchMethodError e) {
      Message.fatal("internal error in icp.core.Main (BootStrap call):" +e);
    }
  }

  /**
   * Reset the permission of one object with the permission from another
   * object. The existing permission on the first object must allow the
   * permission to be reset
   *
   * @param follower object to have permission set.
   * @param leader   object to have permission retrieved.
   */
  public static void samePermissionAs(Object follower, Object leader)
  {
    PermissionSupport.setPermission(follower, 
      PermissionSupport.getPermission(leader));
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

  /**
   * Chain a permission to the permissions already attached to a given object.
   *
   * @param target object to have the permission added to its chain.
   * @param permission permission to add to the chain in the object.
   */
  public static void chainPermission(Object target, Permission permission)
  {
    throw new AssertionError("method not yet implemented");
  }

  /**
   * BootStrap the system for the main thread to get an assigned task.
   *
   * Using a level of indirection allows us to call the bootstrap method
   * and have its class and the classes it uses be loaded by our class
   * loader.
   *
   * Note the access modifier for BootStrap is public. It is public to let the
   * Main.main get access to BootStrap.main. However BootStrap is not visible
   * from outside this class.
   */
  public final static class BootStrap {

    public static void main(String[] args) throws Throwable {

      // Establish an initial task for the main thread
      Task.CURRENT_TASK.set(Task.getFirstInitialTask());

      // invoke the main method of the user's class
      try {

        // Get the Class object associated with the first string in args.
        Class<?> clazz = Class.forName(args[0]);

        // Get the "main" method.
        Method main = clazz.getMethod("main", String[].class);

        // Provide the remainder of the arguments as args to the main method.
        String[] mainArgs = Arrays.copyOfRange(args, 1, args.length);

        // Main is a static method so send null for first argument when calling
        // the main method.
        main.invoke(null, new Object[]{mainArgs});
      }
      catch (IllegalAccessException iae)
      {
        Message.error("IllegalAccessException, " +
          "\"main\" class provided on command line cannot be accessed");
      }
      catch (ClassNotFoundException cnfe)
      {
        Message.error("ClassNotFoundException,  " +
          "\"main\" class provided on command line cannot be found");
      }
      catch (NoSuchMethodException nsme)
      {
        Message.error("NoSuchMethodException, " +
          "\"main\" class provided on command line does not have" +
                " a main method");
      }
      catch (InvocationTargetException ite)
      {
        throw ite.getCause();
      }
    }
  }
}


