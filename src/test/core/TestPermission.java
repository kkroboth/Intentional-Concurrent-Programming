// $Id$
package core;

import icp.core.External;
import icp.core.ICP;
import icp.core.IntentError;
import icp.core.PermissionSupport;
import icp.core.Permissions;
import icp.core.Task;
import icp.wrapper.ICPProxy;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import util.ICPTest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static util.Misc.executeInNewICPTaskThreads;

@External
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
    ICP.setPermission(t, Permissions.getNoAccessPermission());
    assertThrows(IntentError.class, () -> System.out.println(t.x));
  }

  @Test(description = "no access permission (cannot write)")
  public void testNoAccess3() throws Exception {
    TestClass t = new TestClass();
    ICP.setPermission(t, Permissions.getNoAccessPermission());
    assertThrows(IntentError.class, () -> t.x = 42);
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

  @Test(description = "frozen permission (cannot write)")
  public void testFrozen2() throws Exception {
    TestClass t = new TestClass();
    ICP.setPermission(t, Permissions.getFrozenPermission());
    assertThrows(IntentError.class, () -> t.x = 42);
  }

  @Test(description = "samePermissionAs does not copy")
  public void testSetPermissionAs1() throws Exception {
    TestClass t1 = new TestClass();
    TestClass t2 = new TestClass();
    ICP.samePermissionAs(t2, t1);
    ICP.setPermission(t1, Permissions.getFrozenPermission());
    Task task = Task.ofThreadSafe(t2::callAndRead);
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
    assertEquals(executeInNewICPTaskThreads(tasks), nbThreads - 1);
  }

  @Test(description = "transfer permission can be reset")
  public void testTransferReset() throws Exception {
    TestClass shared = new TestClass();
    ICP.setPermission(shared, Permissions.getTransferPermission());
    Task task = Task.ofThreadSafe(() -> {
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
    Task.ofThreadSafe(() -> shared.callAndWrite(42)).run();
    assertThrows(IntentError.class, shared::justCall);
  }

  @Test(description = "owner retains access in loan")
  public void testLoanOwnerRetains() throws Exception {
    TestClass shared = new TestClass();
    ICP.setPermission(shared, Permissions.getLoanPermission());
    Task task = Task.ofThreadSafe(() -> shared.callAndWrite(42));
    task.run(); // OK
    shared.callAndWrite(43); // OK
    assertThrows(IntentError.class, task::run); // fails
  }

  @Test(description = "owner can reset loan")
  public void testOwnerResetLoan() throws Exception {
    TestClass shared = new TestClass();
    ICP.setPermission(shared, Permissions.getLoanPermission());
    Task task = Task.ofThreadSafe(() -> shared.callAndWrite(42));
    task.run(); // OK
    ICP.setPermission(shared, Permissions.getPrivatePermission());
    shared.callAndWrite(43); // OK
    assertThrows(IntentError.class, task::run); // fails
  }

  @Test(description = "other cannot reset loan")
  public void testOtherResetLoan() throws Exception {
    TestClass shared = new TestClass();
    ICP.setPermission(shared, Permissions.getLoanPermission());
    Task task = Task.ofThreadSafe(() ->
      ICP.setPermission(shared, Permissions.getPrivatePermission()));
    assertThrows(IntentError.class, task::run);
  }

  @Test(description = "Proxied permissions")
  public void testProxyPermission() throws Exception {
    Target target = new Target();
    List<Integer> actualList = new ArrayList<>();
    @SuppressWarnings("unchecked") List<Integer> list =
      (List<Integer>) Proxy.newProxyInstance(getClass().getClassLoader(),
        new Class[]{List.class}, new InvocationHandler() {

          {
            ICP.setPermission(this, Permissions.getFrozenPermission());
          }

          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            PermissionSupport.checkCall(target);
            return method.invoke(actualList, args);
          }
        });

    ICP.setPermission(target, Permissions.getHoldsLockPermission(list));
    new Thread(Task.ofThreadSafe(() -> {
      synchronized (list) {
        list.add(10);
      }
    })).start();
  }

  @Test(description = "Test ICPProxy")
  public void testICPProxy() throws Exception {
    List<Integer> actualList = new ArrayList<>();
    List<Integer> list = ICPProxy.newInstance(List.class, actualList,
      (permissionObject, target) -> ICP.setPermission(permissionObject,
        Permissions.getHoldsLockPermission(target)));

    new Thread(Task.ofThreadSafe(() -> {
      synchronized (list) {
        list.add(10);
      }
    })).start();
  }

  private static class Target {
  }
}
