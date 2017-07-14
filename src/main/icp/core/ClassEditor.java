// $Id$

package icp.core;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

import java.util.logging.Logger;

/**
 * Contains static method for doing class editing.  This class cannot be instantiated.
 */
final class ClassEditor {

  private static final Logger logger = Logger.getLogger("icp.core");

  // should not be instantiated
  private ClassEditor() {
    throw new AssertionError("this class cannot be instantiated");
  }

  /**
   * Make edits to a particular class that has been loaded into a javassist classpool.
   *
   * @param cc        Javassist class descriptor for class to be edited
   * @param className name of class to be edited
   */
  static void edit(CtClass cc, String className) {

    // grab all the methods declared in this class
    // and add the check at the beginning of the method body
    CtMethod[] methods = cc.getDeclaredMethods();
    for (CtMethod method : methods) {
      // skip static and abstract methods
      int mods = method.getModifiers();
      if (Modifier.isStatic(mods) || Modifier.isAbstract(mods)) {
        continue;
      }

      // get the method name
      String name = method.getName();

      // HACK to skip methods inserted into class to implement lambdas
      // that capture instance variables.
      // this may only work for the current version of Oracle javac
      // usually lambdas are implemented with static methods, but
      // in some cases they are implemented with virtual methods
      // for instance, if the lambda appears in a virtual method and it
      // captures a variable, then it is implemented with a virtual method
      if (name.startsWith("lambda$")) {
        continue;
      }

      // add the check
      logger.fine("editing method " + method);
      try {
        method.insertBefore(
            "icp.core.PermissionSupport#checkCall($0);");
      } catch (CannotCompileException cce) {
        logger.severe("cannot compile insertion of method checkCall");
        throw new ICPInternalError("cannot compile insertion of method checkCall", cce);
      }
    }

    // now grab all the behaviors (methods, constructors, static initializers)
    // declared in this class and edit their field references in order to
    // insert a check if the access is allowed
    CtBehavior[] behaviors = cc.getDeclaredBehaviors();
    for (CtBehavior behavior : behaviors) {
      logger.fine("got a behavior: " + behavior);
      try {
        // edit its field accesses
        behavior.instrument(
            new ExprEditor() {
              public void edit(FieldAccess fa) throws CannotCompileException {
                String name = fa.getFieldName();

                boolean skip = false;

                // skip fields inserted for captured variables
                // also skip fields that capture outer "this" values
                // THIS IS A HACK dependent on running with javac
                if (name.startsWith("val$") || name.startsWith("this$")) {
                  logger.fine("skip field edit for " + name);
                  skip = true;
                }

                if (skip) {
                  if (fa.isReader()) {
                    fa.replace("{ $_ = $proceed(); }");
                  } else if (fa.isWriter()) {
                    fa.replace("{ $proceed($$); }");
                  } else {
                    // should not reach
                    throw new AssertionError("unreachable");
                  }
                  return;
                }

                // otherwise go ahead and add a check
                if (fa.isReader()) {
                  logger.fine(String.format("edit getField for field %s in class %s",
                      name, cc.getName()));
                  fa.replace("{ " +
                      "icp.core.PermissionSupport#checkGetField($0);" +
                      " $_ = $proceed(); " +
                      "}");
                  return;
                }
                if (fa.isWriter()) {
                  logger.fine(String.format("edit putField for field %s in class %s",
                      name, cc.getName()));
                  fa.replace("{ " +
                      "icp.core.PermissionSupport#checkPutField($0);" +
                      " $proceed($$); " +
                      "}");
                  return;
                }
                // should not reach
                throw new AssertionError("unreachable");
              }
            });
      } catch (CannotCompileException cce) {
        logger.severe("cannot compile insertion of field check");
        throw new ICPInternalError("cannot compile insertion of field check", cce);
      }
    }
  }
}
