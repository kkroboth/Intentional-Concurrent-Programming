package applications.forkjoin;

import applications.forkjoin.shared.Results;
import applications.forkjoin.shared.TextFile;
import applications.forkjoin.shared.WordCount;
import icp.core.ICP;
import icp.core.Task;
import icp.lib.CountDownLatch;

/**
 * Using a countdownlatch for K tasks.
 */
public class CountDownLatchKThreads {
  private final TextFile[] tasks;
  private final CountDownLatch latch;

  // Permission[index]: Latch Permission
  private final Results[] results;


  CountDownLatchKThreads(TextFile[] tasks) {
    this.tasks = tasks;
    latch = new CountDownLatch(tasks.length);
    results = new Results[tasks.length];
  }

  void compute() {
    for (int i = 0; i < tasks.length; i++) {
      final int finalI = i;
      new Thread(Task.ofThreadSafe(() -> {
        latch.registerCountDowner();

        // Create results and add latch permission to ith index
        results[finalI] = new Results();
        ICP.setPermission(results[finalI], latch.getPermission());

        // Compute results
        results[finalI].word = tasks[finalI].word;
        results[finalI].count = WordCount.countWordsInFile(tasks[finalI].open(),
          tasks[finalI].word);

        // count down the latch
        latch.countDown();
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

    CountDownLatchKThreads app = new CountDownLatchKThreads(tasks);
    // Compute
    app.compute();

    Results[] results = app.getResults();
    // Print results
    for (Results result : results) {
      System.out.println("Word: " + result.word + " Count: " + result.count);
    }
  }

}
