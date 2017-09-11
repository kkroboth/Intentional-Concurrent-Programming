package lib;

import icp.core.ICP;
import icp.core.IntentError;
import icp.lib.BinaryLatch;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import util.ICPTest;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.fail;
import static util.Misc.executeInNewICPTaskThreadsFuture;

public class TestBinaryLatch extends ICPTest {

  static class Target {
    AtomicInteger count = new AtomicInteger();

    void call() {
    }

    void incrementCount() {
      count.incrementAndGet();
    }

    int getCount() {
      return count.get();
    }


  }

  // Test the semantics of permissions
  // 4 tests

  @Test(description = "cannot access with close permission if latch open")
  public void closedPermissionCannotAccessWhenOpened() {
    Target target = new Target();
    BinaryLatch latch = new BinaryLatch();
    ICP.setPermission(target, latch.getClosedPermission());

    latch.countDown();
    assertThrows(IntentError.class, target::call);
  }

  @Test(description = "can access with close permission if latch is closed")
  public void closedPermissionCanAccessWhenClosed() {
    Target target = new Target();
    BinaryLatch latch = new BinaryLatch();
    ICP.setPermission(target, latch.getClosedPermission());

    target.call();
  }

  @Test(description = "cannot access with open permission if latch is closed")
  public void openPermissionCannotAccessWhenClosed() {
    Target target = new Target();
    BinaryLatch latch = new BinaryLatch();
    ICP.setPermission(target, latch.getOpenPermission());

    assertThrows(IntentError.class, target::call);
  }

  @Test(description = "can access with open permission if latch is open")
  public void openPermissionCanAccessWhenOpen() {
    Target target = new Target();
    BinaryLatch latch = new BinaryLatch();
    ICP.setPermission(target, latch.getOpenPermission());

    latch.countDown();
    target.call();
  }

  // More general use tests
  // x tests

  @DataProvider
  static Object[][] canAccessWithLockData() {
    return new Object[][]{
      {5},
      {10},
      {100}
    };
  }

  @Test(dataProvider = "canAccessWithLockData")
  public void floodGateTest(int nbThreads) throws InterruptedException, ExecutionException {
    Target target = new Target();
    BinaryLatch latch = new BinaryLatch();
    ICP.setPermission(target, latch.getOpenPermission());

    // Used to fire off all threads
    CountDownLatch ready = new CountDownLatch(nbThreads);

    Runnable r = () -> {
      ready.countDown();

      try {
        latch.await();
        // Can now access data
        target.incrementCount();
      } catch (InterruptedException e) {
        fail("Latch await() was interrupted");
      }
    };


    // Setup
    Runnable[] tasks = new Runnable[nbThreads];
    Arrays.fill(tasks, r);
    Future<Integer> errors = executeInNewICPTaskThreadsFuture(tasks);
    ready.await();

    // Test
    latch.countDown();
    assertEquals(errors.get().intValue(), 0);
  }
}
