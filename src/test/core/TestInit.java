package core;// $Id: DemoSuite.java 17 2017-06-19 16:59:12Z charpov $

import icp.core.FrozenPermission;
import icp.core.ICP;
import icp.core.IntentError;
import org.testng.annotations.Test;

import static core.Utils.executeInNewThread;
import static org.testng.Assert.assertTrue;

public class TestInit {


  @Test(description = "non-ICP thread cannot set permission")
      public void testBogusThreadBootstrap() throws Exception {
    TestClass t1 = new TestClass(42);
    // ensure a first ICP operation from the "main" thread
    ICP.setPermission(t1, FrozenPermission.newInstance());

    Runnable r = () -> {
      TestClass t2 = new TestClass(42);
      ICP.setPermission(t2, FrozenPermission.newInstance());
    };
    assertTrue(executeInNewThread(r) instanceof IntentError);
  }
}
