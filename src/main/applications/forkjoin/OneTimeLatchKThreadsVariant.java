package applications.forkjoin;

import applications.forkjoin.shared.Results;
import applications.forkjoin.shared.TextFile;
import applications.forkjoin.shared.WordCount;
import icp.core.ICP;
import icp.core.Task;
import icp.lib.OneTimeLatchRegistration;
import icp.lib.SimpleReentrantLock;

/**
 * Same as OneTimeLatchKThreads except instead of using an
 * atomic integer for remaining tasks, a reentrant lock guarding
 * integer.
 */
public class OneTimeLatchKThreadsVariant {
  private final TextFile[] tasks;

  // Permission: Reentrant locked permission
  private final MyInteger tasksLeft;

  private final SimpleReentrantLock tasksLeftLock;
  private final OneTimeLatchRegistration latch;

  // Permission[index]: Latch Permission
  private final Results[] results;

  // Container for integer
  @Deprecated()
  // Use icp.core.wrapper.Number instead
  private static class MyInteger {
    public int value;

    public MyInteger(int value) {
      this.value = value;
    }
  }

  OneTimeLatchKThreadsVariant(TextFile[] tasks) {
    this.tasks = tasks;
    latch = new OneTimeLatchRegistration();
    tasksLeftLock = new SimpleReentrantLock();
    results = new Results[tasks.length];
    tasksLeft = new MyInteger(tasks.length);

    /*
     * Potential Problem:
     *
     * int tasksLeft;
     * ICP.setPermission(tasksLeft, permission); // boxed integer
     *
     * If ICP decides to add permissions on all objects, it wil have to either handle
     * boxing and unboxing of primatives differently (custom container) or ignore them
     * and raise errors. This compiles, but isn't allowed since current version doesn't
     * edit jdk classes. Code can box, unbox, and box again with different Integer objects.
     *
     * Autoboxing could use cached Integer objects if range (-128 to 127). Javadoc mentions
     * it *could* cache values outside range.
     * http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/8u40-b25/java/lang/Integer.java#785
     *
     * For current version, tasksLeft must be in a container class.
     */
    // taskLefts guarded-by reentrant lock
    ICP.setPermission(tasksLeft, tasksLeftLock.getLockedPermission());
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
        tasksLeftLock.lock();
        try {
          tasksLeft.value -= 1;
          if (tasksLeft.value == 0) latch.open();
        } finally {
          tasksLeftLock.unlock();
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

    OneTimeLatchKThreadsVariant app = new OneTimeLatchKThreadsVariant(tasks);
    // Compute
    app.compute();

    Results[] results = app.getResults();
    // Print results
    for (Results result : results) {
      System.out.println("Word: " + result.word + " Count: " + result.count);
    }
  }
}
