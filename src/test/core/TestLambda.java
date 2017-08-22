// $Id$
package core;

import icp.core.External;
import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permissions;
import org.testng.annotations.Test;
import util.ICPTest;

import static org.testng.Assert.assertThrows;

// test that lambdas are edited

@External
public class TestLambda extends ICPTest {

  @Test(description = "illegal getField inside lambda")
  public void testLambda1() throws Exception {
    TestClass t = new TestClass();
    ICP.setPermission(t, Permissions.getNoAccessPermission());
    assertThrows(IntentError.class, () -> System.out.println(t.x));
  }

  @Test(description = "illegal putField inside lambda")
  public void testLambda2() throws Exception {
    TestClass t = new TestClass();
    ICP.setPermission(t, Permissions.getFrozenPermission());
    assertThrows(IntentError.class, () -> t.x = 42);
  }
}
