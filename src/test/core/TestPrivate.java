package core;// $Id: DemoSuite.java 17 2017-06-19 16:59:12Z charpov $

import icp.core.AlwaysFailsPermission;
import icp.core.ICP;
import icp.core.IntentError;
import org.testng.annotations.Test;

import static core.Utils.executeInNewThread;
import static org.testng.Assert.assertTrue;

public class TestPrivate {


  @Test(description = "creator has all accesses")
  public void testPrivate1() throws Exception {
    TestClass t = new TestClass(42);
    t.y = t.getX();
    ICP.setPermission(t, AlwaysFailsPermission.newInstance());
  }

  @Test(description = "non creator cannot call")
  public void testPrivate2() throws Exception {
    TestClass t = new TestClass(42);
    assertTrue(executeInNewThread(t::getX) instanceof IntentError);
  }

  @Test(description = "non creator cannot write")
  public void testPrivate3() throws Exception {
    TestClass t = new TestClass(42);
    assertTrue(executeInNewThread(() -> t.y = 42) instanceof IntentError);
  }

  // Note: this test fails if field z is final.  Is it OK?
  @Test(description = "non creator cannot write")
  public void testPrivate4() throws Exception {
    TestClass t = new TestClass(42);
    assert (executeInNewThread(() -> System.out.println(t.z)) instanceof IntentError);
  }
}