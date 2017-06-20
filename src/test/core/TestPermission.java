package core;// $Id$

import icp.core.AlwaysFailsPermission;
import icp.core.FrozenPermission;
import icp.core.ICP;
import icp.core.IntentError;
import org.testng.annotations.Test;

import static core.Utils.executeInNewThread;
import static org.testng.Assert.assertNull;

public class TestPermission {

  @Test(
      description = "no access permission (cannot call/read)",
      expectedExceptions = IntentError.class
  )
  public void testNoAccess1() throws Exception {
    TestClass t = new TestClass(42);
    ICP.setPermission(t, AlwaysFailsPermission.newInstance());
    t.y = t.getX();
  }

  @Test(
      description = "no access permission (cannot set permission)",
      expectedExceptions = IntentError.class
  )
  public void testNoAccess2() throws Exception {
    TestClass t = new TestClass(42);
    ICP.setPermission(t, AlwaysFailsPermission.newInstance());
    ICP.setPermission(t, AlwaysFailsPermission.newInstance());
  }

  @Test(description = "frozen permission (cannot call/read)")
  public void testFrozen1() throws Exception {
    TestClass t = new TestClass(42);
    ICP.setPermission(t, FrozenPermission.newInstance());
    t.getX();
  }

  @Test(
      description = "frozen permission (cannot write)",
      expectedExceptions = IntentError.class
  )
  public void testFrozen2() throws Exception {
    TestClass t = new TestClass(42);
    ICP.setPermission(t, FrozenPermission.newInstance());
    t.y = 0;
  }

  @Test(description = "samePermissionAs does not copy")
  public void testSetPermissionAs1() throws Exception {
    TestClass t1 = new TestClass(42);
    TestClass t2 = new TestClass(42);
    ICP.samePermissionAs(t2, t1);
    ICP.setPermission(t1, FrozenPermission.newInstance());
    assertNull(executeInNewThread(t2::getX));
  }

  @Test(description = "samePermissionAs cannot reset", expectedExceptions = IntentError.class)
  public void testSetPermissionAs2() throws Exception {
    TestClass t1 = new TestClass(42);
    ICP.setPermission(t1, FrozenPermission.newInstance());
    TestClass t2 = new TestClass(42);
    ICP.samePermissionAs(t2, t1);
    ICP.setPermission(t2, AlwaysFailsPermission.newInstance());
  }
}