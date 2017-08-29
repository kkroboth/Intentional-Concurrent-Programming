// $Id$

package icp.core;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.Translator;

import java.util.logging.Logger;

/**
 * Bytecode translator to be called by the Javassist classloader when each class is loaded.
 * <p>
 * The editing of a class is done by <code>ClassEditor.edit</code> static method.
 * <p>
 * <em>Permissions:</em> instances of this class are permanently thread-safe.
 */
final class BytecodeTranslator implements Translator {

  private static final Logger logger = Logger.getLogger("icp.core");

  private static final String libPackage = "icp.lib";

  /**
   * Start the translation process. We have nothing to do at this time.
   *
   * @throws NotFoundException      actually never thrown
   * @throws CannotCompileException actually never thrown
   */
  @Override
  public void start(ClassPool pool) throws NotFoundException, CannotCompileException {
    // cannot use logging here, for mysterious reasons
    // cannot do much here that requires loading of new classes, even with the default loader
  }

  /**
   * Process a class when it is loaded.  A permission field is also added. Furthermore, code is
   * edited to check method calls (on the receiver site) and reads and writes to fields (and the
   * calling site). No permission field is added to interfaces, classes that already inherit such a
   * field, and classes annotated with {@code External}.  However, these are still edited for method
   * calls and read/write of fields.
   *
   * @throws NotFoundException      actually never thrown
   * @throws CannotCompileException actually never thrown
   * @see External
   */
  @Override
  public void onLoad(ClassPool pool, String classname)
    throws NotFoundException, CannotCompileException {
    logger.fine(String.format("thread '%s' loading class '%s'",
      Thread.currentThread().getName(), classname));

    CtClass cc = pool.get(classname);
    PermissionSupport.addPermissionField(cc);
    ClassEditor.edit(cc, classname);
  }
}

