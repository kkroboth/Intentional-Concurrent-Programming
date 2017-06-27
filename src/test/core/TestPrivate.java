package core;// $Id: DemoSuite.java 17 2017-06-19 16:59:12Z charpov $

import icp.core.*;
import org.testng.annotations.Test;
import util.ICPTest;

import static org.testng.Assert.assertThrows;

@DoNotEdit
public class TestPrivate extends ICPTest {

  @Test(description = "creator has all accesses")
  public void testPrivate1() throws Exception {
    TestClass t = new TestClass();
    t.callAndRead();
    t.callAndWrite(42);
    ICP.setPermission(t, Permissions.getNoAccessPermission());
  }

  @Test(description = "non creator cannot call")
  public void testPrivate2() throws Exception {
    TestClass t = new TestClass();
    Task task = Task.fromThreadSafeRunnable(t::justCall);
    assertThrows(IntentError.class, task::run);
  }

  @Test(description = "non creator cannot write")
  public void testPrivate3() throws Exception {
    TestClass t = new TestClass();
    // write cannot be in this class, which is not edited
    Runnable write = new Runnable() { // cannot use lambdas
      public void run() {
        t.x = 42;
      }
    };
    Task task = Task.fromPrivateRunnable(write);
    assertThrows(IntentError.class, task::run);
  }

  // Note: this test fails if field field is final.  Is it OK?
  @Test(description = "non creator cannot read")
  public void testPrivate4() throws Exception {
    TestClass t = new TestClass();
    // read cannot be in this class, which is not edited
    Runnable read = new Runnable() { // cannot use lambdas
      public void run() {
        System.out.println(t.x);
      }
    };
    Task task = Task.fromPrivateRunnable(read);
    assertThrows(IntentError.class, task::run);
  }
}