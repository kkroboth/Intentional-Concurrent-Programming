package applications.forkjoin;

import applications.forkjoin.shared.Results;
import applications.forkjoin.shared.TextFile;
import applications.forkjoin.shared.WordCount;
import icp.core.ICP;
import icp.core.Permissions;
import icp.lib.ICPExecutorService;
import icp.lib.ICPExecutors;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Use ICP thread pool and ICP futures.
 */
public class FutureThreadPool {
  private final TextFile[] tasks;
  private final ICPExecutorService executorService;


  public FutureThreadPool(TextFile[] tasks) {
    this.tasks = tasks;
    executorService = ICPExecutors.newICPExecutorService(
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    );
  }

  Results[] compute() throws ExecutionException, InterruptedException {
    // Fork all futures and get results individually
    Results[] resultsArr = new Results[tasks.length];
    Future<Results>[] futures = new Future[tasks.length];

    // Fork
    for (int i = 0; i < tasks.length; i++) {
      TextFile task = tasks[i];
      Future<Results> future = executorService.submit(() -> {
        Results results = new Results();
        results.word = task.word;
        results.count = WordCount.countWordsInFile(task.open(),
          task.word);
        ICP.setPermission(results, Permissions.getTransferPermission());
        return results;
      });
      futures[i] = future;
    }

    // Join
    for (int i = 0; i < futures.length; i++) {
      resultsArr[i] = futures[i].get();
    }

    executorService.shutdown();
    return resultsArr;
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

    FutureThreadPool app = new FutureThreadPool(tasks);
    // Compute
    Results[] results = app.compute();

    // Print results
    for (Results result : results) {
      System.out.println("Word: " + result.word + " Count: " + result.count);
    }
  }
}
