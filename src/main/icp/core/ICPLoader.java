// $Id$

package icp.core;

import javassist.ClassPool;
import javassist.Loader;
import javassist.Translator;
import javassist.NotFoundException;;
import javassist.CannotCompileException;;

import java.util.logging.Logger;

/**
 *  ICP's Javassist class loader.
 */
public class ICPLoader extends Loader {

  /**
   *  Construct the ICP class loader, which will include a bytecode editor.
   *  This constructor is called automatically at system startup by passing
   *  -Djava.system.class.loader=icp.core.ICPLoader to the java command.
   *
   *  @param parent the parent (default) class loader
   */
  public ICPLoader(ClassLoader parent)
  {
    // apparently critical to use this constructor in order to avoid
    // recursive invocation of java.lang.ClassLoader constructor
    super(parent, null);

    // need to attach bytecode editor to the loader
    Translator t = new BytecodeTranslator();
    ClassPool pool = ClassPool.getDefault();
    try {
      this.addTranslator(pool, t);
    } catch (NotFoundException | CannotCompileException e) {
      // seems unlikely that I can call this method because my class loader
      // is not fully constructed?
      System.err.println(
        "internal error in icp.core.Main (addTranslator call):" +e);
      System.exit(-1);
    }
  }
}

