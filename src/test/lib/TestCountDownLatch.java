package lib;

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

  @Test(description = "cannot call await after countdown")
  public void cannotCallAwaitAfterCountDown() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    latch.registerCountDowner();
    latch.countDown();
    assertThrows(IntentError.class, latch::await);
  }

  @Test(description = "Cannot countdown past zero")
  public void cannotCountDownPastZero() {
    CountDownLatch latch = new CountDownLatch(1);
    latch.registerCountDowner();
    latch.countDown();
    assertThrows(IntentError.class, latch::countDown);
  }

  @Test(description = "Cannot register more countdowners than count")
  public void cannotRegisterMoreCountdownersThanCount() {
    CountDownLatch latch = new CountDownLatch(1);
    latch.registerCountDowner();
    assertThrows(IntentError.class, latch::registerCountDowner);
  }

  //   Note: TestNG creates a thread to monitor timeout?
//   Throws error thread "TestNGInvoker-awaitOnZeroReturns() is not a task"
  @Test(enabled = false, description = "await on zero immediately returns", timeOut = 1)
  public void awaitOnZeroReturns() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(0);
    latch.await();

    latch = new CountDownLatch(1);
    latch.countDown();
    latch.await();
  }

//   Test the semantics of permissions

  @Test(description = "task can access as countdowner")
  public void closedPermissionOnInitialCount() {
    Target target = new Target();
    CountDownLatch latch = new CountDownLatch(5);
    ICP.setPermission(target, latch.getPermission());
    latch.registerCountDowner();

    target.call();
  }

  @Test(description = "task cannot access if not registered")
  public void noregistrationIntentError() {
    Target target = new Target();
    CountDownLatch latch = new CountDownLatch(1);
    ICP.setPermission(target, latch.getPermission());

    assertThrows(IntentError.class, target::call);
  }

  @Test(description = "cannot access with close permission on open latch")
  public void cannotClosedPermission() {
    Target target = new Target();
    CountDownLatch latch = new CountDownLatch(1);
    ICP.setPermission(target, latch.getPermission());
    latch.registerCountDowner();
    latch.countDown();

    assertThrows(IntentError.class, target::call);
  }
}
