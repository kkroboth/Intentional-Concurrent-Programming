package applications.forkjoin;

import applications.forkjoin.shared.TextFile;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.CountDownLatch;

import java.util.Arrays;

/**
 * Using a countdownlatch for K textFiles.
 */
public class CountDownLatchKThreads {
  // Permission[index]: Latch Permission
  private final TextFile[] textFiles;
  private final CountDownLatch latch;


  CountDownLatchKThreads(TextFile[] textFiles) {
    this.textFiles = textFiles;
    latch = new CountDownLatch(textFiles.length);
  }

  void compute() {
    for (int i = 0; i < textFiles.length; i++) {
      final int finalI = i;
      new Thread(Task.ofThreadSafe(() -> {
        latch.registerCountDowner();

        // Create results and add latch permission to ith index
        TextFile textFile = textFiles[finalI];
        ICP.setPermission(textFile, latch.getPermission());

        // Compute results
        textFile.run();

        // count down the latch
        latch.countDown();
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

    CountDownLatchKThreads app = new CountDownLatchKThreads(textFiles);
    // Compute
    app.compute();
    app.awaitComputation();

    // Print results
    for (TextFile textFile : textFiles) {
      System.out.println("Word: " + textFile.word + " Count: " + textFile.getCount());
    }
  }

}
