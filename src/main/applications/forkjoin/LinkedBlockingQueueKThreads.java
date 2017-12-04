package applications.forkjoin;

import applications.forkjoin.shared.Results;
import applications.forkjoin.shared.TextFile;
import applications.forkjoin.shared.WordCount;
import icp.core.ICP;
import icp.core.Permissions;
import icp.lib.ICPExecutors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Allows master to retrieve results as they come in.
 */
public class LinkedBlockingQueueKThreads {
  private final TextFile[] tasks;
  private final LinkedBlockingQueue<Results> queue;
  private final ExecutorService executorService;


  LinkedBlockingQueueKThreads(TextFile[] tasks) {
    this.tasks = tasks;
    queue = new LinkedBlockingQueue<>();
    executorService = ICPExecutors.newICPExecutorService(
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    );
  }

  void compute() {
    for (TextFile task : tasks) {
      executorService.execute(() -> {
        Results results = new Results();
        results.word = task.word;
        results.count = WordCount.countWordsInFile(task.open(),
          task.word);

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

        ICP.setPermission(results, Permissions.getTransferPermission());
        queue.offer(results);
      });
    }

    executorService.shutdown();
  }

  BlockingQueue<Results> getResultQueue() {
    return queue;
  }

  public static void main(String[] args) throws InterruptedException {
    TextFile[] tasks = new TextFile[100];
    for (int i = 0; i < 100; i++) {
      tasks[i] = new TextFile("alice.txt", "the");
    }
    LinkedBlockingQueueKThreads app = new LinkedBlockingQueueKThreads(tasks);

    app.compute();

    BlockingQueue<Results> queue = app.getResultQueue();
    for (int i = 0; i < 100; i++) {
      Results result = queue.take();
      System.out.println("Word: " + result.word + " Count: " + result.count);
    }
  }

}
