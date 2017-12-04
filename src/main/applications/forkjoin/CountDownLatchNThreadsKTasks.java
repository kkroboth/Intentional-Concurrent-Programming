package applications.forkjoin;

import applications.Utils;
import applications.forkjoin.shared.Results;
import applications.forkjoin.shared.TextFile;
import applications.forkjoin.shared.WordCount;
import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.CountDownLatch;
import icp.wrapper.Number;
import issues.Misc;

/**
 * Master waits via countdown latch and workers
 * processes multiple jobs.
 * <p>
 * N Threads and K jobs.
 */
public class CountDownLatchNThreadsKTasks {
  private final TextFile[] tasks;
  private final Number nextIndex; // guarded-by: itself (could be this)
  private final CountDownLatch latch;
  private final Results[] results; // Each element guarded-by latch

  public CountDownLatchNThreadsKTasks(TextFile[] tasks, int threads) {
    this.tasks = tasks;
    nextIndex = Number.zero();
    latch = new CountDownLatch(tasks.length);
    results = new Results[tasks.length];

    ICP.setPermission(nextIndex, Permissions.getHoldsLockPermission(nextIndex));
    //ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  void workerThread() {
    // each worker continues to grab another job
    Thread thread = new Thread(Task.ofThreadSafe(() -> {
      latch.registerCountDowner();

      for (; ; ) {
        int taskIndex = 0;

          synchronized (nextIndex) {
            try {
              // there is an error here -- can't be caught
              // TODO: Use -1 to signal done. Use Optional. Use method.
              if (nextIndex.val == tasks.length) break;
              taskIndex = nextIndex.val;
              nextIndex.val += 1;
            } catch (Throwable e) {
              e.printStackTrace();
            }
          }

        // See OneTimeLatchKThreads for why the writes to array can be read correctly in master
        results[taskIndex] = new Results();
        ICP.setPermission(results[taskIndex], latch.getPermission());
        results[taskIndex].word = tasks[taskIndex].word;
        results[taskIndex].count = WordCount.countWordsInFile(tasks[taskIndex].open(),
          tasks[taskIndex].word);
        latch.countDown();
      }
    }));
    thread.setUncaughtExceptionHandler(Utils.logThreadException());
    thread.start();
  }

  void compute() {
    for (int i = 0; i < tasks.length; i++) {
      workerThread();
    }
  }

  Results[] getResults() throws InterruptedException {
    latch.registerWaiter();
    latch.await();
    return results;
  }

  public static void main(String[] args) throws InterruptedException {
//    Misc.setupConsoleLogger();

    TextFile[] tasks = new TextFile[100];
    for (int i = 0; i < 100; i++) {
      tasks[i] = new TextFile("alice.txt", "the");
    }
    CountDownLatchNThreadsKTasks app = new CountDownLatchNThreadsKTasks(tasks, 1);

    app.compute();
    Results[] results = app.getResults();

    for (Results result : results) {
      System.out.println("Word: " + result.word + " Count: " + result.count);
    }
  }
}
