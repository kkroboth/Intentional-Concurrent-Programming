package applications.forkjoin;

import applications.forkjoin.shared.Results;
import applications.forkjoin.shared.TextFile;
import applications.forkjoin.shared.WordCount;
import icp.core.ICP;
import icp.core.Task;
import icp.lib.OneTimeLatchRegistration;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Using OneTimeLatch and AtomicInteger for
 * K Threads on K Tasks.
 */
public class OneTimeLatchKThreads {
  private final TextFile[] tasks;
  private final OneTimeLatchRegistration latch;
  private final AtomicInteger tasksLeft;

  // Permission[index]: Latch Permission
  private final Results[] results;


  OneTimeLatchKThreads(TextFile[] tasks) {
    this.tasks = tasks;
    latch = new OneTimeLatchRegistration();
    results = new Results[tasks.length];
    tasksLeft = new AtomicInteger(tasks.length);

    /* Note:
     *
     * Can't put permissions on arrays (Special object).
     * Instead a wrapper container around the array must be used.
     */
    //ICP.setPermission(results, latch.getPermission());
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
        if (tasksLeft.decrementAndGet() == 0)
          latch.open();

      /* Potential Problem avoided:
       *
       * Originally thought the latch (j.u.c CountDownLatch) provided a
       * happens-before guarantee to the waiter which can see all writes
       * from worker threads in each array's index. I did not fully understand
       * why it worked and I (the user) do not know what I'm doing.
       *
       * Hence, case and point of using ICP!
       *
       * Prove: For every task there is a synchronized-with relationship of
       * writing to array => DecrementAndGet (total-order of every CAS).
       * For the last DecrementAndGet that reaches zero and opens the latch,
       * there's another synchronized-with relation open latch => await latch.
       * That forms a happens-before on all array writes and accesses by the transitive
       * closure synchronized-with relations and program order of each thread.
       * All writes in workers happen-before reads in master.
       * Therefore, program is correct.
       *
       * Can't check this in ICP though...
       */
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

    OneTimeLatchKThreads app = new OneTimeLatchKThreads(tasks);
    // Compute
    app.compute();

    Results[] results = app.getResults();
    // Print results
    for (Results result : results) {
      System.out.println("Word: " + result.word + " Count: " + result.count);
    }
  }

}
