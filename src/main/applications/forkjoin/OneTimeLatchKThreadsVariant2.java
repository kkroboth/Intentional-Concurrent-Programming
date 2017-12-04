package applications.forkjoin;

import applications.forkjoin.shared.Results;
import applications.forkjoin.shared.TextFile;
import applications.forkjoin.shared.WordCount;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.OneTimeLatchRegistration;

/**
 * Same as OneTimeLatchKThreads except instead of using an
 * atomic integer for remaining tasks, and monitor lock to guard int.
 */
public class OneTimeLatchKThreadsVariant2 {
  private final TextFile[] tasks;

  // Permission: Holds lock permission (tasksLeftLock)
  private final MyInteger tasksLeft;

  private final Object tasksLeftLock;
  private final OneTimeLatchRegistration latch;

  // Permission[index]: Latch Permission
  private final Results[] results;

  // Container for integer
  private static class MyInteger {
    public int value;

    public MyInteger(int value) {
      this.value = value;
    }
  }

  OneTimeLatchKThreadsVariant2(TextFile[] tasks) {
    this.tasks = tasks;
    latch = new OneTimeLatchRegistration();
    tasksLeftLock = new Object();
    results = new Results[tasks.length];
    tasksLeft = new MyInteger(tasks.length);

    // guard tasks left with monitor lock
    ICP.setPermission(tasksLeft, Permissions.getHoldsLockPermission(tasksLeftLock));
  }

  void compute() {
    for (int i = 0; i < tasks.length; i++) {
      final int finalI = i;
      new Thread(Task.ofThreadSafe(() -> {
        latch.registerOpener();

        // Create results and add latch permission to ith index
        results[finalI] = new Results();
        ICP.setPermission(results[finalI], latch.getPermission());

        // Compute results
        results[finalI].word = tasks[finalI].word;
        results[finalI].count = WordCount.countWordsInFile(tasks[finalI].open(),
          tasks[finalI].word);

        // If task is the last to compute, open the latch
        synchronized (tasksLeftLock) {
          tasksLeft.value -= 1;
          if (tasksLeft.value == 0) latch.open();
        }
      })).start();
    }
  }

  Results[] getResults() throws InterruptedException {
    latch.registerWaiter();
    latch.await();
    return results;
  }

  public static void main(String[] args) throws InterruptedException {
    TextFile[] tasks = new TextFile[]{
      new TextFile("alice.txt", "the"),
      new TextFile("alice.txt", "alice"),
      new TextFile("alice.txt", "I"),
    };

    OneTimeLatchKThreadsVariant2 app = new OneTimeLatchKThreadsVariant2(tasks);
    // Compute
    app.compute();

    Results[] results = app.getResults();
    // Print results
    for (Results result : results) {
      System.out.println("Word: " + result.word + " Count: " + result.count);
    }
  }
}
