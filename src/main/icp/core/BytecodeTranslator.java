// $Id$

package icp.core;

import javassist.*;

import icp.lib.Task;

import java.util.logging.Logger;

/**
 * Bytecode translator to be called by the Javassist classloader when each
 * class is loaded.
 *
 * Classes from <code>javassist</code>, the ICP core package and selected
 * classes from the ICP lib are skipped and not edited.
 *
 * The editing of a class is done by <code>ClassEditor.edit</code> static
 * method.
 *
 */
final class BytecodeTranslator implements Translator {

  private static final Logger logger = Logger.getLogger("icp.core");

  private static final String libPackage = "icp.lib";

  /**
   * Class names from the ICP lib that should not be edited by javassist.
   *
   * I don't follow convention here for enum names because it is being
   * used for its toString method, and ease of listing elements.
   */
  private enum DoNotEdit {
    TaskLocal, Thread;

    @Override
    public String toString(){
      return libPackage + "." + super.toString();
    }
  }

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
    // apparently cannot call the logger here because of infinite
    // recursion in the construction of the class loaders
    //logger.fine("Translator start method is returning");
    // but I can call System.err.println
    //System.err.println("Translator start method is returning");
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
    // so do not edit the classes in icp.core
    if (classname.startsWith("icp.core"))
    {
      return;
    }

    // also do not want to edit selected classes in icp.lib
    // using startsWith for all of the classes in lib that should not be edited.
    // this will take care of the root class as well as any inner classes.
    for (DoNotEdit clazzName : DoNotEdit.values()) {
      if (classname.startsWith(clazzName.toString())) {
        return;
      }
    }

    logger.fine(String.format("checking and editing class %s", classname));

    CtClass cc = pool.get(classname);
    PermissionSupport.addPermissionField(cc);
    ClassEditor.edit(cc, classname);
  }

}

