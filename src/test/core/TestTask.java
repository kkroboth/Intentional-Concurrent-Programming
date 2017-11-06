package core;

import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permissions;
import icp.core.Task;
import icp.core.TaskThread;
import icp.lib.OneTimeLatch;
import org.testng.annotations.Test;
import util.ICPTest;

import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;


public class TestTask extends ICPTest {

  static class Target {
    void call() {

    }
  }

  static class SafeTarget {
    AtomicInteger count = new AtomicInteger();

    void call() {
      count.getAndIncrement();
    }

    public int getCount() {
      return count.get();
    }
  }


  @Test()
  void testTaskCreation() throws InterruptedException {
    Target o = new Target();
    ICP.setPermission(o, Permissions.getThreadSafePermission());

    Task task = Task.ofThreadSafe(() -> {
      o.call();
    });

    Task task2 = Task.ofPrivate(new Runnable() {

      @Override
      public void run() {
        o.call();
      }
    });

    Thread t1 = new Thread(task);
    Thread t2 = new Thread(task2);

    t1.start();
    t2.start();
    t1.join();
    t2.join();
  }

  @Test(description = "TaskThread join targets")
  void testTaskThreadJoin() throws InterruptedException {
    int nbTargets = 10;

    // Create list of targets which are worked on
    SafeTarget[] targets = new SafeTarget[nbTargets];
    for (int i = 0; i < nbTargets; i++) {
      targets[i] = new SafeTarget();
      // Transfer permission to task
      ICP.setPermission(targets[i], Permissions.getTransferPermission());
    }

    // Create task
    Task task = Task.ofThreadSafe(() -> {
      for (int i = 0; i < nbTargets; i++) {
        targets[i].call();
      }
    });

    TaskThread taskThread = new TaskThread(new Thread(task), task);
    taskThread.start((Object[]) targets);

    taskThread.join();
    for (int i = 0; i < nbTargets; i++) {
      // Able to call after join
      assertEquals(targets[i].getCount(), 1);
    }

  }

  @Test(description = "TaskThread fails because using Thread.join()")
  void taskThreadJoinFails() throws InterruptedException {
    SafeTarget target = new SafeTarget();
    ICP.setPermission(target, Permissions.getTransferPermission());

    Task task = Task.ofThreadSafe(target::call);
    Thread thread = new Thread(task);
    TaskThread taskThread = new TaskThread(thread, task);

    taskThread.start(target);
    thread.join(); // bad, should use taskThread.join()

    assertThrows(IntentError.class, target::getCount);
  }

  // TODO
  @Test(enabled = false, description = "TaskThreadGroup fork-join")
  void taskThreadGroupJoin() {
    int nbTasks = 10;
    SafeTarget target = new SafeTarget();

    // Use our own OneTimeLatch with AtomicInteger
    AtomicInteger left = new AtomicInteger(nbTasks);
    OneTimeLatch latch = new OneTimeLatch();
    ICP.setPermission(target, latch.getIsOpenPermission());
  }
}
