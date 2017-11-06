package lib;

import icp.core.IntentError;
import icp.core.Task;
import icp.lib.OneTimeLatch;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import util.ICPTest;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static icp.core.ICP.setPermission;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static util.Misc.executeInNewICPTaskThread;

public class TestOneTimeLatch extends ICPTest {
  static class Target {
    final AtomicInteger count = new AtomicInteger();

    void call() {
      // no op
    }

    void increment() {
      count.getAndIncrement();
    }

    int getCount() {
      return count.get();
    }
  }

  // This is now allowed
  @Test(enabled = false, description = "cannot call open() more than once")
  public void cannotCallOpenMoreThanOnce() throws InterruptedException {
    OneTimeLatch latch = new OneTimeLatch();
    latch.open();

    assertThrows(IntentError.class, latch::open);
    assertTrue(executeInNewICPTaskThread(latch::open) instanceof IntentError);
  }

  @Test(description = "same task cannot call await after open")
  public void cannotCallAwaitAfterOpen() {
    OneTimeLatch latch = new OneTimeLatch();
    latch.open();
    assertThrows(IntentError.class, latch::await);
  }

  @Test(description = "must register await for isOpen permission")
  public void mustRegisterAwaitForPermission() throws InterruptedException {
    OneTimeLatch latch = new OneTimeLatch();
    Target target = new Target();
    setPermission(target, latch.getIsOpenPermission());
    latch.open(); // Latch is opened, but permission requires registration

    assertTrue(getCount(latch) == 0);
    assertTrue(executeInNewICPTaskThread(target::call) instanceof IntentError);
  }

  @DataProvider
  static Object[][] awaitsOnLatchData() {
    return new Object[][]{
      {5}, {10}, {100}
    };
  }

  @Test(description = "successful awaits on latch", dataProvider = "awaitsOnLatchData")
  public void awaitsOnLatch(int nbThreads) throws InterruptedException, ExecutionException {
    Target target = new Target();
    OneTimeLatch latch = new OneTimeLatch();
    AtomicInteger errors = new AtomicInteger();

    setPermission(target, latch.getIsOpenPermission());
    final int finalCount = nbThreads;
    Task[] runnables = new Task[nbThreads];
    for (int i = 0; i < runnables.length; i++) {
      runnables[i] = Task.ofThreadSafe(() -> {
        try {
          latch.await();
          target.increment();
        } catch (InterruptedException e) {
          fail("interrupted", e);
        } catch (IntentError e) {
          e.printStackTrace();
          errors.incrementAndGet();
        }
      });

      new Thread(runnables[i], "worker-" + i).start();
    }

    // Tasks
    latch.open();
    for (Task runnable : runnables) {
      runnable.join();
    }
    assertEquals(errors.get(), 0);
    // TODO: Fails -- Permission does not reset
    assertEquals(finalCount, target.getCount());
  }

  private long getCount(OneTimeLatch latch) {
    try {
      Field field = OneTimeLatch.class.getDeclaredField("latch");
      field.setAccessible(true);
      java.util.concurrent.CountDownLatch countDownLatch = (java.util.concurrent.CountDownLatch) field.get(latch);
      return countDownLatch.getCount();
    } catch (NoSuchFieldException | IllegalAccessException e) {
      fail("Could not get internal count", e);
    }

    return -1;
  }

}



