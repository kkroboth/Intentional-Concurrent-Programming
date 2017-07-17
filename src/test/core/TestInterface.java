// $Id$
package core;

import icp.core.IntentError;
import icp.core.ICP;
import icp.core.Permissions;

public interface TestInterface {
  default void meth1() {
    TestClass t = new TestClass();
    ICP.setPermission(t, Permissions.getNoAccessPermission());
    // field references should be checked in this method
    System.out.println(t.x);
  }
  default void meth2() {
    TestClass t = new TestClass();
    ICP.setPermission(t, Permissions.getNoAccessPermission());
    // field references should be checked in this method
    t.x = 42;
  }
  default void meth3() {
  }
}

