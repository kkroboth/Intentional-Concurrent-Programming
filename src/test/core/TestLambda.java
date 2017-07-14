// $Id$
package core;

import icp.core.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import util.ICPTest;

import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static util.Misc.executeInNewICPThreads;

// test that lambdas are edited
// since this class is not edited, the lambdas are placed
// inside anonymous classes, which will be edited

@DoNotEdit
public class TestLambda extends ICPTest {

  @Test(description = "illegal getField inside lambda")
  public void testLambda1() throws Exception {
    TestClass t = new TestClass();
    Runnable outer = new Runnable() { 
      public void run() {
        Runnable read = () -> System.out.println(t.x);
        ICP.setPermission(t, Permissions.getNoAccessPermission());
        assertThrows(IntentError.class, read::run);
      }
    };
    outer.run();
  }

  @Test(description = "illegal putField inside lambda")
  public void testLambda2() throws Exception {
    TestClass t = new TestClass();
    Runnable outer = new Runnable() { 
      public void run() {
        Runnable write = () -> t.x = 42;
        ICP.setPermission(t, Permissions.getFrozenPermission());
        assertThrows(IntentError.class, write::run);
      }
    };
    outer.run();
  }

}
