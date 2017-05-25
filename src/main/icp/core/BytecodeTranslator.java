// $Id$

package icp.core;

import javassist.*;

import icp.lib.Task;

import java.util.logging.Logger;

/**
 * Bytecode translator to be called by the Javassist classloader when each
 * class is loaded.
 *
 * Classes from <code>javassist</code> and the ICP core package are
 * skipped and not edited.
 *
 * The editing of a class is done by <code>ClassEditor.edit</code> static
 * method.
 *
 */
final class BytecodeTranslator implements Translator {

  private static final Logger logger = Logger.getLogger("icp.core");

  /**
   * Start the translation process. We have nothing to do at this time.
   *
   * @throws NotFoundException actually never thrown
   * @throws CannotCompileException actually never thrown
   */
  @Override
  public void start(ClassPool pool)
    throws NotFoundException, CannotCompileException
  {
    logger.fine("Translator start method is returning");
  }

  /**
   * Process a class when it is loaded. Ignore javassist classes
   * and the core ICP classes.
   *
   * @throws NotFoundException actually never thrown
   * @throws CannotCompileException actually never thrown
   */
  @Override
  public void onLoad(ClassPool pool, String classname)
    throws NotFoundException, CannotCompileException
  {
    logger.fine(String.format("loading class %s", classname));

    // do not want to edit any of the javassist classes
    if (classname.startsWith("javassist.")) return;

    // need to avoid infinite recursion
    // so do not edit the classes in icp.core below
    if (classname.startsWith("icp.core"))
    {
      return;
    }

    logger.fine(String.format("checking and editing class %s", classname));

    CtClass cc = pool.get(classname);
    PermissionSupport.addPermissionField(cc);
    ClassEditor.edit(cc, classname);
  }

}

