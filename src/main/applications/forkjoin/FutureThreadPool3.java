package applications.forkjoin;

import applications.forkjoin.shared.TextFile;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.ICPExecutorService;
import icp.lib.ICPExecutors;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Use ICP thread pool and ICP futures.
 * <p>
 * Uses join permission on executor.
 */
public class FutureThreadPool3 {
  private final TextFile[] textFiles;
  private final ICPExecutorService executorService;


  public FutureThreadPool3(TextFile[] textFiles) {
    this.textFiles = textFiles;
    executorService = ICPExecutors.newICPExecutorService(
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    );
  }

  void compute() {
    // Fork
    for (int i = 0; i < textFiles.length; i++) {
      int finalI = i;
      executorService.submit(Task.ofThreadSafe(() -> {
        TextFile textFile = textFiles[finalI];
        textFile.run();
        ICP.setPermission(textFile, executorService.getAwaitTerminationPermission());
      }));
    }

    executorService.shutdown();
  }

  void awaitComputation() throws InterruptedException {
    executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
  }

  public static void main(String[] args) throws InterruptedException {
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
    Arrays.stream(textFiles).forEach(t -> ICP.setPermission(t, Permissions.getTransferPermission()));
    FutureThreadPool3 app = new FutureThreadPool3(textFiles);
    // Compute
    app.compute();
    app.awaitComputation();

    // Print results
    // Print results
    for (TextFile textFile : textFiles) {
      System.out.println("Word: " + textFile.word + " Count: " + textFile.getCount());
    }
  }
}
