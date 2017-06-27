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

@DoNotEdit
public class TestPermission extends ICPTest {

  @Test(description = "no access permission (cannot call)")
  public void testNoAccess1() throws Exception {
    TestClass t = new TestClass();
    ICP.setPermission(t, Permissions.getNoAccessPermission());
    assertThrows(IntentError.class, t::justCall);
  }

  @Test(description = "no access permission (cannot read)")
  public void testNoAccess2() throws Exception {
    TestClass t = new TestClass();
    // read cannot be in this class, which is not edited
    Runnable read = new Runnable() { // cannot use lambdas
      public void run() {
        System.out.println(t.x);
      }
    };
    ICP.setPermission(t, Permissions.getNoAccessPermission());
    ICP.setPermission(read, Permissions.getFrozenPermission());
    assertThrows(IntentError.class, read::run);
  }

  @Test(description = "no access permission (cannot write)")
  public void testNoAccess3() throws Exception {
    TestClass t = new TestClass();
    // write cannot be in this class, which is not edited
    Runnable write = new Runnable() { // cannot use lambdas
      public void run() {
        t.x = 42;
      }
    };
    ICP.setPermission(t, Permissions.getNoAccessPermission());
    ICP.setPermission(write, Permissions.getFrozenPermission());
    assertThrows(IntentError.class, write::run);
  }

  @Test(description = "no access permission (cannot set permission)")
  public void testNoAccess4() throws Exception {
    TestClass t = new TestClass();
    ICP.setPermission(t, Permissions.getNoAccessPermission());
    assertThrows(IntentError.class, () ->
        ICP.setPermission(t, Permissions.getNoAccessPermission()));
  }

  @Test(description = "frozen permission (can call/read)")
  public void testFrozen1() throws Exception {
    TestClass t = new TestClass();
    ICP.setPermission(t, Permissions.getFrozenPermission());
    t.callAndRead();
  }

  @Test(
      description = "frozen permission (cannot write)",
      expectedExceptions = IntentError.class
  )
  public void testFrozen2() throws Exception {
    TestClass t = new TestClass();
    // write cannot be in this class, which is not edited
    Runnable write = new Runnable() { // cannot use lambdas
      public void run() {
        t.x = 42;
      }
    };
    ICP.setPermission(t, Permissions.getFrozenPermission());
    ICP.setPermission(write, Permissions.getFrozenPermission());
    write.run();
  }

  @Test(description = "samePermissionAs does not copy")
  public void testSetPermissionAs1() throws Exception {
    TestClass t1 = new TestClass();
    TestClass t2 = new TestClass();
    ICP.samePermissionAs(t2, t1);
    ICP.setPermission(t1, Permissions.getFrozenPermission());
    Task task = Task.fromThreadSafeRunnable(t2::callAndRead);
    task.run();
  }

  @Test(description = "samePermissionAs cannot reset")
  public void testSetPermissionAs2() throws Exception {
    TestClass t1 = new TestClass();
    ICP.setPermission(t1, Permissions.getFrozenPermission());
    TestClass t2 = new TestClass();
    ICP.samePermissionAs(t2, t1);
    assertThrows(IntentError.class, () ->
        ICP.setPermission(t2, Permissions.getNoAccessPermission()));
  }

  @DataProvider
  static Object[][] testTransferSafeData() {
    return new Object[][]{
        {10},
        {100},
        {1000}
    };
  }

  @Test(description = "transfer permission is thread-safe", dataProvider = "testTransferSafeData")
  public void testTransferSafe(int nbThreads) throws Exception {
    TestClass shared = new TestClass();
    ICP.setPermission(shared, Permissions.getTransferPermission());
    Runnable r = shared::justCall;
    Runnable[] tasks = new Runnable[nbThreads];
    Arrays.fill(tasks, r);
    assertEquals(executeInNewICPThreads(tasks), nbThreads - 1);
  }

  @Test(description = "transfer permission can be reset")
  public void testTransferReset() throws Exception {
    TestClass shared = new TestClass();
    ICP.setPermission(shared, Permissions.getTransferPermission());
    Task task = Task.fromThreadSafeRunnable(() -> {
      ICP.setPermission(shared, Permissions.getFrozenPermission());
      shared.callAndRead();
      assertThrows(IntentError.class, () -> shared.callAndWrite(42));
    });
    task.run();
    shared.callAndRead(); // OK since frozen
    assertThrows(IntentError.class, () -> shared.callAndWrite(42)); // fails
  }

  @Test(description = "owner loses ownership in transfer")
  public void testTransferOwnerLoses() throws Exception {
    TestClass shared = new TestClass();
    ICP.setPermission(shared, Permissions.getTransferPermission());
    Task.fromThreadSafeRunnable(() -> shared.callAndWrite(42)).run();
    assertThrows(IntentError.class, shared::justCall);
  }

  @Test(description = "owner retains access in loan")
  public void testLoanOwnerRetains() throws Exception {
    TestClass shared = new TestClass();
    ICP.setPermission(shared, Permissions.getLoanPermission());
    Task task = Task.fromThreadSafeRunnable(() -> shared.callAndWrite(42));
    task.run(); // OK
    shared.callAndWrite(43); // OK
    assertThrows(IntentError.class, task::run); // fails
  }

  @Test(description = "owner can reset loan")
  public void testOwnerResetLoan() throws Exception {
    TestClass shared = new TestClass();
    ICP.setPermission(shared, Permissions.getLoanPermission());
    Task task = Task.fromThreadSafeRunnable(() -> shared.callAndWrite(42));
    task.run(); // OK
    ICP.setPermission(shared, Permissions.getPrivatePermission());
    shared.callAndWrite(43); // OK
    assertThrows(IntentError.class, task::run); // fails
  }

  @Test(description = "other cannot reset loan")
  public void testOtherResetLoan() throws Exception {
    TestClass shared = new TestClass();
    ICP.setPermission(shared, Permissions.getLoanPermission());
    Task task = Task.fromThreadSafeRunnable(() ->
        ICP.setPermission(shared, Permissions.getPrivatePermission()));
    assertThrows(IntentError.class, task::run);
  }
}
