package applications.latches;

import icp.core.ICP;
import icp.core.Task;
import icp.core.TaskThread;
import icp.core.TaskThreadGroup;
import icp.lib.OneTimeLatch;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Set up multiple threads and then use a OneTimeLatch to start them all
 * at once.
 * <p>
 * <em>Note:</em> We use a java.util CountDownLatch to wait until all threads are setup.
 */
public class ThreadStart {

  // Shared between all threads
  private static class SharedOperation {
    final AtomicInteger computations = new AtomicInteger();

    void compute() {
      computations.getAndIncrement();
    }

    int getCount() {
      return computations.get();
    }
  }

  public static void main(String[] args) throws InterruptedException {
    if (args.length == 0) {
      System.out.println("Usage: ThreadStart <nbThreads>");
    }

    final int nbThreads = Integer.parseInt(args[0]);
    SharedOperation operation = new SharedOperation();

    // Setup threads
    CountDownLatch waitSetup = new CountDownLatch(nbThreads);
    OneTimeLatch startLatch = new OneTimeLatch();

    // Only use operation if latch has been opened
    ICP.setPermission(operation, startLatch.getIsOpenPermission());

    // Threads
    TaskThreadGroup threadGroup;
    TaskThread[] threads = new TaskThread[nbThreads];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = TaskThread.of(Task.ofThreadSafe(() -> {
        waitSetup.countDown();

        // Wait for main thread
        try {
          startLatch.await();
          operation.compute();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

      }));
    }

    threadGroup = new TaskThreadGroup(threads);
    threadGroup.start(operation);

    // Start
    waitSetup.await(); // Normal countdownlatch (java.util)
    startLatch.open();
    threadGroup.join();

    if (operation.getCount() != nbThreads) {
      throw new RuntimeException("Computations did not add up");
    }
    System.out.println("All threads did their computations!");
  }
}
