// $Id$

package lib;

import icp.core.*;
import icp.core.Thread;
import icp.lib.SimpleReentrantLock;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import util.ICPTest;

import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static util.Misc.executeInNewICPThread;
import static util.Misc.executeInNewICPThreads;

@DoNotEdit
public class TestReentrantLock extends ICPTest {

  static class Target {

    private int called;

    public void call() {
      called += 1;
    }

    public int getCallCount() {
      return called;
    }
  }

  @Test(
      description = "creator thread cannot access without lock",
      expectedExceptions = IntentError.class
  )
  public void cannotAccessWithoutLock() throws Exception {
    Target target = new Target();
    SimpleReentrantLock lock = new SimpleReentrantLock();
    ICP.setPermission(target, lock.getLockedPermission());
    target.call();
  }

  @Test(
      description = "creator thread cannot reset permission",
      expectedExceptions = IntentError.class
  )
  public void cannotResetPermission1() throws Exception {
    Target target = new Target();
    SimpleReentrantLock lock = new SimpleReentrantLock();
    ICP.setPermission(target, lock.getLockedPermission());
    ICP.setPermission(target, Permissions.getFrozenPermission());
  }

  @Test(
      description = "creator thread cannot reset permission, even with lock",
      expectedExceptions = IntentError.class
  )
  public void cannotResetPermission2() throws Exception {
    Target target = new Target();
    SimpleReentrantLock lock = new SimpleReentrantLock();
    ICP.setPermission(target, lock.getLockedPermission());
    try {
      lock.lock();
      ICP.setPermission(target, Permissions.getFrozenPermission());
    } finally {
      lock.unlock();
    }
  }

  @DataProvider
  static Object[][] canAccessWithLockData() {
    return new Object[][]{
        {5, 10},
        {10, 100},
        {100, 1000}
    };
  }

  @Test(
      description = "any thread can access with lock; lock works as a lock",
      dataProvider = "canAccessWithLockData"
  )
  public void canAccessWithLock(int nbThreads, int nbLoops) throws Exception {
    Target target = new Target();
    SimpleReentrantLock lock = new SimpleReentrantLock();
    ICP.setPermission(target, lock.getLockedPermission());

    Runnable r = () -> {
      for (int i = 0; i < nbLoops; i++) {
        lock.lock();
        try {
          target.call();
        } finally {
          lock.unlock();
        }
      }
    };

    Runnable[] tasks = new Runnable[nbThreads];
    Arrays.fill(tasks, r);
    assertEquals(executeInNewICPThreads(tasks), 0);
    lock.lock();
    try {
      assertEquals(target.getCallCount(), nbThreads * nbLoops);
    } finally {
      lock.unlock();
    }
  }

  @Test(description = "tasks own, not threads", expectedExceptions = IntentError.class)
  public void testThreadsDontOwn() throws Exception {
    SimpleReentrantLock lock = new SimpleReentrantLock();
    Task t1 = Task.fromThreadSafeRunnable(lock::lock);
    Task t2 = Task.fromThreadSafeRunnable(lock::unlock);
    t1.run();
    t2.run();
  }

  @Test(description = "tasks own across threads")
  public void testTasksOwn() throws Exception {
    SimpleReentrantLock lock = new SimpleReentrantLock();
    Task t = Task.fromPrivateRunnable(new Runnable() {
      boolean first = true;

      public void run() {
        if (first) {
          lock.lock();
          first = false;
        } else {
          lock.unlock();
        }
      }
    });
    // don't use executeInNewICPThread in first run to avoid memory barriers
    new Thread(t).start(); // ICP thread, but Java thread would work too
    Thread.sleep(1000);
    // does not throw IntentError because the task does own the lock
    // throws IllegalMonitorStateException instead because the thread does not own the lock
    assert (executeInNewICPThread(t) instanceof IllegalMonitorStateException);
  }
}