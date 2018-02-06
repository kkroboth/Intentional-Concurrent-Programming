package applications.forkjoin;

import applications.forkjoin.shared.TextFile;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.ICPExecutorService;
import icp.lib.ICPExecutors;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * Use ICP thread pool and ICP futures.
 */
public class FutureThreadPool {
  private final TextFile[] textFiles;
  private final ICPExecutorService executorService;


  public FutureThreadPool(TextFile[] textFiles) {
    this.textFiles = textFiles;
    executorService = ICPExecutors.newICPExecutorService(
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    );
  }

  void compute() throws ExecutionException, InterruptedException {
    // Fork all futures
    Task[] tasks = new Task[textFiles.length];

    // Fork
    for (int i = 0; i < textFiles.length; i++) {
      TextFile textFile = textFiles[i];
      int finalI = i;
//      tasks[i] = Task.ofThreadSafe(() -> {
//        textFile.run();
//      });
      tasks[i] = Task.ofThreadSafe(textFile);
      ICP.setPermission(textFile, tasks[finalI].getJoinPermission());
      executorService.execute(tasks[i]);
    }

    // Join
    for (Task task : tasks) {
      task.join();
    }

    executorService.shutdown();
  }

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    TextFile[] textFiles = new TextFile[]{
      new TextFile("alice.txt", "the"),
      new TextFile("alice.txt", "alice"),
      new TextFile("alice.txt", "I"),
      new TextFile("alice.txt", "a"),
      new TextFile("alice.txt", "wonderland"),
      new TextFile("alice.txt", "cheese"),
      new TextFile("alice.txt", "cat"),
      new TextFile("alice.txt", "cats"),
      new TextFile("alice.txt", "kill"),
    };

    // Transfer the array of text files
//    Arrays.stream(textFiles).forEach(t -> ICP.setPermission(t, Permissions.getTransferPermission()));

    FutureThreadPool app = new FutureThreadPool(textFiles);
    // Compute
    app.compute();

    // Print results
    for (TextFile textFile : textFiles) {
      System.out.println("Word: " + textFile.word + " Count: " + textFile.getCount());
    }
  }
}
