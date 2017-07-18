// $Id$

package icp.core;

import javassist.*;

import java.util.logging.Logger;

/**
 * Bytecode translator to be called by the Javassist classloader when each class is loaded.
 *
 * The editing of a class is done by <code>ClassEditor.edit</code> static method.
 *
 * <em>Permissions:</em> instances of this class are permanently thread-safe.
 *
 */
final class BytecodeTranslator implements Translator {

  private static final Logger logger = Logger.getLogger("icp.core");

  private static final String libPackage = "icp.lib";

  /**
   * Start the translation process. We have nothing to do at this time.
   *
   * @throws NotFoundException actually never thrown
   * @throws CannotCompileException actually never thrown
   */
  @Override
  public void start(ClassPool pool) throws NotFoundException, CannotCompileException
  {
    // cannot use logging here, for mysterious reasons
    // cannot do much here that requires loading of new classes, even with the default loader
  }

  /**
   * Process a class when it is loaded.  Interfaces, abstract classes and classes annotated with
   * {@code DoNotEdit} are not edited.
   *
   * @see DoNotEdit
   *
   * @throws NotFoundException actually never thrown
   * @throws CannotCompileException actually never thrown
   */
  @Override
  public void onLoad(ClassPool pool, String classname)
    throws NotFoundException, CannotCompileException
  {
    logger.fine(String.format("thread '%s' loading class '%s'",
        Thread.currentThread().getName(), classname));

    CtClass cc = pool.get(classname);
    if (mustEdit(cc)) {
      // do not add try to add a field to an interface
      if (!cc.isInterface())
      {
        PermissionSupport.addPermissionField(cc);
      }
      ClassEditor.edit(cc, classname);
    }
  }

  private static Boolean mustEdit(CtClass cc) throws NotFoundException {
    logger.fine(String.format("checking class '%s' for possible editing", cc.getName()));
    // skip DoNotEdit classes
    try {
      if (cc.getAnnotation(DoNotEdit.class) != null)
        return false;
    } catch (ClassNotFoundException e) {
      throw new NotFoundException("class not found", e);
    }
    logger.fine(String.format("class '%s' ready to edit", cc.getName()));
    return true;
  }
}

