package applications.forkjoin;

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
  // Note: What did I mean by this?
  //
  // Permission: Latch permission
  private final TextFile textFile;
  private final OneTimeLatchRegistration latch;

  OneTimeLatchOneThreadOneTask(TextFile textFile) {
    this.textFile = textFile;
    latch = new OneTimeLatchRegistration();
    ICP.setPermission(this.textFile, latch.getPermission());

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
      textFile.setCount(WordCount.countWordsInFile(textFile.open(), textFile.word));
      latch.open();
    })).start();
  }

  void awaitComputation() throws InterruptedException {
    latch.registerWaiter();
    latch.await();
  }

  public static void main(String[] args) throws InterruptedException {
    TextFile textFile = new TextFile("alice.txt", "alice");
    OneTimeLatchOneThreadOneTask app = new OneTimeLatchOneThreadOneTask(textFile);
    app.compute();
    app.awaitComputation();

    System.out.println("Word: " + textFile.word + " Count: " + textFile.getCount());
  }

}
