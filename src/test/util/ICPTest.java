// $Id$
package util;

/**
 * Extending this class enforces a check that the proper ICPLoader is being used.
 */
public abstract class ICPTest {

  protected ICPTest() {
    ClassLoader loader = getClass().getClassLoader();
    if (loader.getClass() != icp.core.ICPLoader.class)
      throw new Error("no ICP loader; aborting");
  }
}