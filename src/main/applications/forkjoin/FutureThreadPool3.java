package applications.forkjoin;

import applications.forkjoin.shared.Results;
import applications.forkjoin.shared.TextFile;
import applications.forkjoin.shared.WordCount;
import icp.core.ICP;
import icp.lib.ICPExecutorService;
import icp.lib.ICPExecutors;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Use ICP thread pool and ICP futures.
 * <p>
 * Uses join permission on executor.
 */
public class FutureThreadPool3 {
  private final TextFile[] tasks;
  private final ICPExecutorService executorService;
  private final Results[] results;


  public FutureThreadPool3(TextFile[] tasks) {
    this.tasks = tasks;
    this.results = new Results[tasks.length];
    executorService = ICPExecutors.newICPExecutorService(
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    );
  }

  void compute() throws ExecutionException, InterruptedException {
    // Fork
    for (int i = 0; i < tasks.length; i++) {
      int finalI = i;
      executorService.submit(() -> {
        TextFile task = tasks[finalI];
        Results results = new Results();
        results.word = task.word;
        results.count = WordCount.countWordsInFile(task.open(),
          task.word);
        ICP.setPermission(results, executorService.getAwaitTerminationPermission());
        this.results[finalI] = results;
      });
    }

    executorService.shutdown();
  }

  Results[] getResults() throws InterruptedException {
    executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    return results;
  }

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    TextFile[] tasks = new TextFile[]{
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

    FutureThreadPool3 app = new FutureThreadPool3(tasks);
    // Compute
    app.compute();
    Results[] results = app.getResults();

    // Print results
    for (Results result : results) {
      System.out.println("Word: " + result.word + " Count: " + result.count);
    }
  }
}
