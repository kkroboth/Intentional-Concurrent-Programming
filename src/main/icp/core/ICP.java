// $Id$

package icp.core;

import icp.lib.Task;

import javassist.ClassPool;
import javassist.Loader;
import javassist.Translator;
import javassist.NotFoundException;;
import javassist.CannotCompileException;;

import java.util.logging.Logger;

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
 *
 * Class must be public because we require user app to call ICP.initialize.
 */
final public class ICP {

  private static final Logger logger = Logger.getLogger("icp.core");

  /** Initialize ICP: create a class loader that will edit
   *  user bytecode; establish the initial Task for the main thread.
   */
  public static void initialize() {
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
      cl.run("icp.core.ICP$BootStrap", new String[0]);
    } catch (Throwable e) {
      Message.fatal("internal error in icp.core.Main (BootStrap call):" +e);
    }
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

    public static void main(String[] args) {

      // Establish an initial task for the main thread
      Task.CURRENT_TASK.set(Task.getFirstInitialTask());
    }
  }
}


