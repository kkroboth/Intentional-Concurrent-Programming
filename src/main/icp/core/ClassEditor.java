// $Id$

package icp.core;

import javassist.*;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

import java.util.logging.Logger;

/**
 * Contains static methods for doing class editing.
 */
final class ClassEditor
{
  private static final Logger logger = Logger.getLogger("icp.core");

  // should not be instantiated
  private ClassEditor() {}

  /** Make edits to a particular class that has been loaded into a
   *  javassist classpool.
   *
   *  @param cc        Javassist class descriptor for class to be edited
   *  @param className name of class to be edited
   */
  static void edit(CtClass cc, String className)
  {
    // grab all the constructors declared in the class
    // and iterate through and edit the constructor calls in each
    CtConstructor[] constructors = cc.getDeclaredConstructors();
    for (CtConstructor constructor : constructors)
    {
      // getLongName displays the constructpor's class and the parameter types
      String longName = constructor.getLongName();
      logger.finer(String.format("processing behavior: long name is %s;",
        longName));

      try {
        // edit its constructor calls
        constructor.instrument(
          new ExprEditor() {
            public void edit(ConstructorCall c)
              throws CannotCompileException
            {
              // only interested in calls to super
              if (c.isSuper())
              {
                logger.fine("edit call on super");
                c.replace("{ $proceed($$); " +
                  " icp.core.PermissionSupport#initialize($0); }");
              }
              else
              {
                // just leave the constructor call alone
                // apparently must call replace even though no edit is needed
                c.replace(" $proceed($$);");
              }
            }
          });
      }
      catch(CannotCompileException ex)
      {
        logger.severe("cannot compile edit of constructor");
        Message.fatal("cannot compile edit of constructor");
      }
    }

    // now grab all the methods declared in this class
    // and add the check at the beginning of the method body
    CtMethod[] methods = cc.getDeclaredMethods();
    for (CtMethod method : methods)
    {
      // skip static and abstract methods
      int mods = method.getModifiers();
      if (Modifier.isStatic(mods) || Modifier.isAbstract(mods))
      {
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
      if (name.startsWith("lambda$"))
      {
        continue;
      }

      // add the check
      logger.fine("editing method " + method);
      try {
        method.insertBefore(
          "icp.core.PermissionSupport#checkCall($0);");
      }
      catch (CannotCompileException cce)
      {
        logger.severe("cannot compile insertion of method checkCall");
        Message.fatal("cannot compile insertion of method checkCall");
      }
    }

    // now grab all the behaviors (methods, constructors, static initializers)
    // declared in this class and edit their field references in order to
    // insert a check if the access is allowed
    CtBehavior[] behaviors = cc.getDeclaredBehaviors();
    for (CtBehavior behavior : behaviors)
    {
      logger.fine("got a behavior: " + behavior);
      try {
        // edit its field accesses
        behavior.instrument(
          new ExprEditor() {
            public void edit(FieldAccess fa)
              throws CannotCompileException
            {
              String name = fa.getFieldName();

              // skip fields inserted for captured variables
              // also skip fields that capture outer "this" values
              // THIS IS A HACK dependent on running with javac
              if (name.startsWith("val$") ||
                  name.startsWith("this$"))
              {
                logger.fine("skip field edit for " + name);
                if (fa.isReader())
                {
                  fa.replace("{ $_ = $proceed(); }");
                }
                else if (fa.isWriter())
                {
                  fa.replace("{ $proceed($$); }");
                }
                else
                {
                  // should not reach
                  assert(false);
                }
                return;
              }

              // otherwise go ahead and add a check
              if (fa.isReader())
              {
                logger.fine("edit getField for " + name);
                fa.replace("{ " +
                  "icp.core.PermissionSupport#checkGetField($0);" +
                  " $_ = $proceed(); " +
                  "}");
                return;
              }
              if (fa.isWriter())
              {
                logger.fine("edit putField for " + name);
                fa.replace("{ " +
                  "icp.core.PermissionSupport#checkPutField($0);" +
                  " $proceed($$); " +
                  "}");
                return;
              }
              // should not reach
              assert(false);
            }
          });
      }
      catch(CannotCompileException cce)
      {
        logger.severe("cannot compile insertion of field check");
        Message.fatal("cannot compile insertion of field check");
      }
    }
  }
}

