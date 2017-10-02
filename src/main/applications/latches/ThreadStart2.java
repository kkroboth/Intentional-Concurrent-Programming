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
 *
 * Will always throw intent error because isOpen permission is not resettable
 */
public class ThreadStart2 {
  private final CountDownLatch waitLatch;
  private final OneTimeLatch startLatch;
  private final int nbThreads;

  private final SharedOperation operation;
  private final TaskThread[] threads;
  private final TaskThreadGroup threadGroup;

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

  public ThreadStart2(int nbThreads) {
    this.nbThreads = nbThreads;
    waitLatch = new CountDownLatch(nbThreads);
    operation = new SharedOperation();
    startLatch = new OneTimeLatch();
    ICP.setPermission(operation, startLatch.getIsOpenPermission());

    // Setup threads
    this.threads = new TaskThread[nbThreads];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = TaskThread.of(Task.fromThreadSafeRunnable(() -> {
        waitLatch.countDown();

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
  }

  public int startAndGetCount() throws InterruptedException {
    waitLatch.await(); // Normal countdownlatch (java.util)
    startLatch.open();
    threadGroup.join();

    if (operation.getCount() != nbThreads) {
      throw new RuntimeException("Computations did not add up");
    }

    return operation.getCount();
  }

  public static void main(String[] args) throws InterruptedException {
    if (args.length == 0) {
      System.out.println("Usage: ThreadStart <nbThreads>");
    }

    final int nbThreads = Integer.parseInt(args[0]);

    ThreadStart2 app = new ThreadStart2(nbThreads);
    int operations = app.startAndGetCount();

    if (operations != nbThreads) {
      throw new RuntimeException("Computations did not add up");
    }
    System.out.println("All threads did their computations!");
  }
}
