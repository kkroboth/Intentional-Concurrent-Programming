package applications.forkjoin;

import applications.forkjoin.shared.TextFile;
import applications.forkjoin.shared.WordCount;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.OneTimeLatchRegistration;

import java.util.Arrays;

/**
 * Same as OneTimeLatchKThreads except instead of using an
 * atomic integer for remaining textFiles, and monitor lock to guard int.
 */
public class OneTimeLatchKThreadsVariant2 {
  // Permission[index]: Latch Permission
  private final TextFile[] textFiles;

  // Permission: Holds lock permission (tasksLeftLock)
  private final MyInteger tasksLeft;

  private final Object tasksLeftLock;
  private final OneTimeLatchRegistration latch;

  // Container for integer
  private static class MyInteger {
    public int value;

    public MyInteger(int value) {
      this.value = value;
    }
  }

  OneTimeLatchKThreadsVariant2(TextFile[] textFiles) {
    this.textFiles = textFiles;
    latch = new OneTimeLatchRegistration();
    tasksLeftLock = new Object();
    tasksLeft = new MyInteger(textFiles.length);

    // guard textFiles left with monitor lock
    ICP.setPermission(tasksLeft, Permissions.getHoldsLockPermission(tasksLeftLock));
  }

  void compute() {
    for (int i = 0; i < textFiles.length; i++) {
      final int finalI = i;
      new Thread(Task.ofThreadSafe(() -> {
        latch.registerOpener();

        // Create results and add latch permission to ith index
        TextFile textFile = textFiles[finalI];
        ICP.setPermission(textFile, latch.getPermission());

        // Compute results
        textFile.setCount(WordCount.countWordsInFile(textFile.open(),
          textFile.word));

        // If task is the last to compute, open the latch
        synchronized (tasksLeftLock) {
          tasksLeft.value -= 1;
          if (tasksLeft.value == 0) latch.open();
        }
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

    OneTimeLatchKThreadsVariant2 app = new OneTimeLatchKThreadsVariant2(textFiles);
    // Compute
    app.compute();
    app.awaitComputation();

    // Print results
    for (TextFile textFile : textFiles) {
      System.out.println("Word: " + textFile.word + " Count: " + textFile.getCount());
    }
  }
}
