// $Id$

package icp.core;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.Loader;
import javassist.NotFoundException;
import javassist.Translator;

/**
 * ICP's Javassist class loader.
 */
public class ICPLoader extends Loader {

  /**
   * Construct the ICP class loader, which will include a bytecode editor.
   * This constructor is called automatically at system startup by passing
   * -Djava.system.class.loader=icp.core.ICPLoader to the java command.
   *
   * @param parent the parent (default) class loader
   */
  public ICPLoader(ClassLoader parent) {
    // apparently critical to use this constructor in order to avoid
    // recursive invocation of java.lang.ClassLoader constructor
    super(parent, null);

    // do not want to edit any of the javassist classes
    delegateLoadingOf("javassist.");

    // need to avoid infinite recursion, so do not edit the classes in icp.core
    delegateLoadingOf("icp.core.");

    // keeping intelliJ, sbt and TestNG out; should use a list as a property
    delegateLoadingOf("sbt.");
    delegateLoadingOf("org.testng.");
    delegateLoadingOf("com.intellij.rt.");

    // need to attach bytecode editor to the loader
    Translator t = new BytecodeTranslator();
    ClassPool pool = ClassPool.getDefault();
    try {
      this.addTranslator(pool, t);
    } catch (NotFoundException | CannotCompileException e) {
      throw new ICPInternalError("cannot create ICP loader", e);
    }
  }
}

