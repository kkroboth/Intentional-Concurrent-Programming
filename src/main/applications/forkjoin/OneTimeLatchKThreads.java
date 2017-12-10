package applications.forkjoin;

import applications.forkjoin.shared.TextFile;
import applications.forkjoin.shared.WordCount;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.OneTimeLatchRegistration;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Using OneTimeLatch and AtomicInteger for
 * K Threads on K Tasks.
 */
public class OneTimeLatchKThreads {
  // Permission[index]: Latch Permission
  private final TextFile[] textFiles;
  private final OneTimeLatchRegistration latch;
  private final AtomicInteger tasksLeft;


  OneTimeLatchKThreads(TextFile[] textFiles) {
    this.textFiles = textFiles;
    latch = new OneTimeLatchRegistration();
    tasksLeft = new AtomicInteger(textFiles.length);

    /* Note:
     *
     * Can't put permissions on arrays (Special object).
     * Instead a wrapper container around the array must be used.
     */
    //ICP.setPermission(textFiles, latch.getPermission());
  }

  void compute() {
    for (int i = 0; i < textFiles.length; i++) {
      final int finalI = i;
      new Thread(Task.ofThreadSafe(() -> {
        latch.registerOpener();

        // Add latch permission to ith index
        TextFile textFile = textFiles[finalI];
        ICP.setPermission(textFile, latch.getPermission());

        // Compute results
        textFile.setCount(WordCount.countWordsInFile(textFile.open(),
          textFile.word));

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

  void awaitComputation() throws InterruptedException {
    latch.registerWaiter();
    latch.await();
  }

  public static void main(String[] args) throws InterruptedException {
    TextFile[] textFiles = new TextFile[]{
      new TextFile("alice.txt", "the"),
      new TextFile("alice.txt", "alice"),
      new TextFile("alice.txt", "I"),
    };

    // Transfer the array of text files
    Arrays.stream(textFiles).forEach(t -> ICP.setPermission(t, Permissions.getTransferPermission()));

    OneTimeLatchKThreads app = new OneTimeLatchKThreads(textFiles);
    // Compute
    app.compute();
    app.awaitComputation();

    // Print results
    for (TextFile textFile : textFiles) {
      System.out.println("Word: " + textFile.word + " Count: " + textFile.getCount());
    }
  }
}
