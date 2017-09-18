package lib;

import icp.core.External;
import icp.core.ICP;
import icp.core.IntentError;
import icp.lib.CountDownLatch;
import org.testng.annotations.Test;
import util.ICPTest;

import static org.testng.Assert.assertThrows;

public class TestCountDownLatch extends ICPTest {

  static class Target {
    void call() {
    }
  }

  // Test semantics of latch
  // 2 tests

  @Test(description = "cannot initiate with negative count")
  public void exceptionOnNegativeCount() {
    assertThrows(IllegalArgumentException.class, () -> new CountDownLatch(-1));
  }

  // Note: TestNG creates a thread to monitor timeout?
  // Throws error thread "TestNGInvoker-awaitOnZeroReturns() is not a task"
  @Test(enabled = false, description = "await on zero immediately returns", timeOut = 1)
  public void awaitOnZeroReturns() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(0);
    latch.await();

    latch = new CountDownLatch(1);
    latch.countDown();
    latch.await();
  }

  // Test the semantics of permissions
  // 6 tests

  @Test(description = "can access with close permission on latch with initial count")
  public void closedPermissionOnInitialCount() {
    Target target = new Target();
    CountDownLatch latch = new CountDownLatch(5);
    ICP.setPermission(target, latch.getClosedPermission());

    target.call();
  }

  @Test(description = "cannot access with close permission on open latch")
  public void cannotClosedPermission() {
    Target target = new Target();
    CountDownLatch latch = new CountDownLatch(5);
    ICP.setPermission(target, latch.getClosedPermission());
    for (int i = 0; i < 5; i++) {
      latch.countDown();
    }

    assertThrows(IntentError.class, target::call);
  }

  @Test(description = "can access with open permission on open latch")
  public void openPermissionOnOpenLatch() {
    Target target = new Target();
    CountDownLatch latch = new CountDownLatch(1);
    ICP.setPermission(target, latch.getOpenPermission());
    latch.countDown();

    target.call();
  }

  @Test(description = "cannot access with open permission on closed latch")
  public void cannotOpenPermissionClosedLatch() {
    Target target = new Target();
    CountDownLatch latch = new CountDownLatch(1);
    ICP.setPermission(target, latch.getOpenPermission());

    assertThrows(IntentError.class, target::call);
  }

  @Test(description = "can access with intermediate permission on non-initial count")
  public void canIntermediatePermissionNonInitialCount() {
    Target target = new Target();
    CountDownLatch latch = new CountDownLatch(2);
    ICP.setPermission(target, latch.getIntermediatePermission());
    latch.countDown();

    target.call();
  }

  @Test(description = "cannot access with intermediate permission")
  public void cannotIntermediatePermission() {
    Target target = new Target();
    CountDownLatch latch = new CountDownLatch(2);
    ICP.setPermission(target, latch.getIntermediatePermission());

    assertThrows(IntentError.class, target::call);

    latch.countDown();
    latch.countDown();
    assertThrows(IntentError.class, target::call);
  }
}
