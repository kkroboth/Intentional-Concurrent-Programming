package applications.tasks;

import applications.Utils;
import icp.core.External;
import icp.core.ICP;
import icp.core.Task;
import icp.lib.SimpleReentrantLock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UnmatchedUnlockBetweenJobs {
  static class Shared {
    int value;
  }

  void startThreads() throws InterruptedException {
    Lock lock = new ReentrantLock();
    Semaphore order = new Semaphore(0);
    Shared shared = new Shared();

    Runnable r1 = () -> {
      // Lock but don't release the lock
      lock.lock();
      try {
        shared.value = 1;
      } finally {
        order.release();
      }
    };

    Runnable r2 = () -> {
      // lock and release the lock
      order.acquireUninterruptibly();
      lock.lock();
      try {
        shared.value = 2;
      } finally {
        // I still had the lock from before
        lock.unlock();
        lock.unlock();
      }
    };

    ExecutorService executor = Executors.newSingleThreadExecutor(Utils.logExceptionThreadFactory());
    executor.execute(r1);
    executor.execute(r2);
    executor.shutdown();
    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
  }

  void startTask() throws InterruptedException {
    SimpleReentrantLock lock = new SimpleReentrantLock();
    Semaphore order = new Semaphore(2);
    Shared shared = new Shared();
    ICP.setPermission(shared, lock.getLockedPermission());

    Runnable r1 = () -> {
      // Lock but don't release the lock
      lock.lock();
      try {
        shared.value = 1;
      } finally {
        order.release();
        // no unlock -- bad
      }
    };

    Runnable r2 = () -> {
      // release r1's lock
      order.acquireUninterruptibly();
      try {
        shared.value = 2; // IntentException! Not owner
      } finally {
        lock.unlock(); // from r1
      }
    };

    ExecutorService executor = Executors.newSingleThreadExecutor(Utils.logExceptionThreadFactory());
    executor.execute(Task.ofThreadSafe(r1));
    executor.execute(Task.ofThreadSafe(r2));
    executor.shutdown();
    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
  }

  public static void main(String[] args) throws InterruptedException {
    UnmatchedUnlockBetweenJobs app = new UnmatchedUnlockBetweenJobs();
    app.startTask();
  }
}
