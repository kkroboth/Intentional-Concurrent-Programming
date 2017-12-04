package applications.forkjoin;

import applications.forkjoin.shared.Results;
import applications.forkjoin.shared.TextFile;
import applications.forkjoin.shared.WordCount;
import icp.core.ICP;
import icp.core.Task;
import icp.lib.OneTimeLatchRegistration;

/**
 * Using a OneTimeLatch and One thread.
 */
public class OneTimeLatchOneThreadOneTask {
  // Final fields do not require any permission associated with
  // class (*this*).
  private final TextFile task;
  private final OneTimeLatchRegistration latch;

  // Permission: Latch permission
  private final Results results;

  OneTimeLatchOneThreadOneTask(TextFile task) {
    this.task = task;
    latch = new OneTimeLatchRegistration();
    results = new Results();
    ICP.setPermission(results, latch.getPermission());

    /* Note:
     *
     * Setting *this* object permission to frozen isn't required as long as all the
     * fields accessed in another thread are declared final.
     *
     * Sometimes I (user) forget this and add the permission anyways. The permission would
     * never be used since editing of classes doesn't put the check on final fields.
     */
    //ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  void compute() {
    new Thread(Task.ofThreadSafe(() -> {
      latch.registerOpener();
      results.word = task.word;
      results.count = WordCount.countWordsInFile(task.open(), task.word);
      latch.open();
    })).start();
  }

  Results getResults() throws InterruptedException {
    latch.registerWaiter();
    latch.await();
    return results;
  }

  public static void main(String[] args) throws InterruptedException {
    OneTimeLatchOneThreadOneTask app = new OneTimeLatchOneThreadOneTask(
      new TextFile("alice.txt", "alice")
    );
    app.compute();

    Results results = app.getResults();
    System.out.println("Word: " + results.word + " Count: " + results.count);
  }

}
