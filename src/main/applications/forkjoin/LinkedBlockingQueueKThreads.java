package applications.forkjoin;

import applications.forkjoin.shared.TextFile;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.ICPExecutorService;
import icp.lib.ICPExecutors;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Allows master to retrieve results as they come in.
 */
public class LinkedBlockingQueueKThreads {
  private final TextFile[] textFiles;
  private final LinkedBlockingQueue<TextFile> queue;
  private final ICPExecutorService executorService;


  LinkedBlockingQueueKThreads(TextFile[] textFiles) {
    this.textFiles = textFiles;
    queue = new LinkedBlockingQueue<>();
    executorService = ICPExecutors.newICPExecutorService(
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    );
  }

  void compute() {
    for (TextFile textFile : textFiles) {
      executorService.execute(Task.ofThreadSafe(() -> {
        textFile.run();

        // What permission should be used here?
        // Transfer?

        /* Note:
         *
         * Must be careful how the transfer permission is put on since collections, maps, queues,
         * etc, might call methods which transfer it back to original task. For example, a hashmap
         * will call hashcode() which is overridden in a class.
         *
         * However if the transfer was put after, the user *must* not be able to access it beforehand.
         * With a blocking queue, once the payload is offered, you cannot then put on the permission.
         */

        ICP.setPermission(textFile, Permissions.getTransferPermission());
        queue.offer(textFile);
      }));
    }

    executorService.shutdown();
  }

  BlockingQueue<TextFile> getResultQueue() {
    return queue;
  }

  public static void main(String[] args) throws InterruptedException {
    TextFile[] textFiles = new TextFile[100];
    for (int i = 0; i < 100; i++) {
      textFiles[i] = new TextFile("alice.txt", "the");
    }
    // Transfer the array of text files
    Arrays.stream(textFiles).forEach(t -> ICP.setPermission(t, Permissions.getTransferPermission()));

    LinkedBlockingQueueKThreads app = new LinkedBlockingQueueKThreads(textFiles);

    app.compute();

    BlockingQueue<TextFile> queue = app.getResultQueue();
    for (int i = 0; i < 100; i++) {
      TextFile result = queue.take();
      System.out.println("Word: " + result.word + " Count: " + result.getCount());
    }
  }

}
