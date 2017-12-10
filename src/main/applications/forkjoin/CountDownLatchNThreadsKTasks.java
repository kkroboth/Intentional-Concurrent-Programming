package applications.forkjoin;

import applications.Utils;
import applications.forkjoin.shared.TextFile;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.CountDownLatch;
import icp.wrapper.Number;

/**
 * Master waits via countdown latch and workers
 * processes multiple jobs.
 * <p>
 * N Threads and K jobs.
 */
public class CountDownLatchNThreadsKTasks {
  private final TextFile[] textFiles; // Each element guarded-by latch
  private final Number nextIndex; // guarded-by: itself (could be this)
  private final CountDownLatch latch;
  private final int nbThreads;

  public CountDownLatchNThreadsKTasks(TextFile[] textFiles, int threads) {
    this.nbThreads = threads;
    this.textFiles = textFiles;
    nextIndex = Number.zero();
    latch = new CountDownLatch(textFiles.length);

    ICP.setPermission(nextIndex, Permissions.getHoldsLockPermission(nextIndex));
  }

  void workerThread() {
    // each worker continues to grab another job
    Thread thread = new Thread(Task.ofThreadSafe(() -> {
      latch.registerCountDowner();

      for (; ; ) {
        int taskIndex;

        synchronized (nextIndex) {
          // there is an error here -- can't be caught
          // TODO: Use -1 to signal done. Use Optional. Use method.
          if (nextIndex.val == textFiles.length) break;
          taskIndex = nextIndex.val;
          nextIndex.val += 1;
        }

        // See OneTimeLatchKThreads for why the writes to array can be read correctly in master
        TextFile textFile = textFiles[taskIndex];
        ICP.setPermission(textFile, latch.getPermission());
        textFile.run();
        latch.countDown();
      }
    }));
    thread.setUncaughtExceptionHandler(Utils.logThreadException());
    thread.start();
  }

  void compute() {
    for (int i = 0; i < nbThreads; i++) {
      workerThread();
    }
  }

  void awaitComputation() throws InterruptedException {
    latch.registerWaiter();
    latch.await();
  }

  public static void main(String[] args) throws InterruptedException {
//    Misc.setupConsoleLogger();
    TextFile[] textFiles = new TextFile[100];
    for (int i = 0; i < 100; i++) {
      textFiles[i] = new TextFile("alice.txt", "the");
      ICP.setPermission(textFiles[i], Permissions.getTransferPermission());
    }

    CountDownLatchNThreadsKTasks app = new CountDownLatchNThreadsKTasks(textFiles, 1);

    app.compute();
    app.awaitComputation();

    // Print results
    for (TextFile textFile : textFiles) {
      System.out.println("Word: " + textFile.word + " Count: " + textFile.getCount());
    }
  }
}
