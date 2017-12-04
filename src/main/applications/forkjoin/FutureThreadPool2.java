package applications.forkjoin;

import applications.forkjoin.shared.Results;
import applications.forkjoin.shared.TextFile;
import applications.forkjoin.shared.WordCount;
import icp.core.ICP;
import icp.core.Permissions;
import icp.lib.ICPExecutorService;
import icp.lib.ICPExecutors;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Use ICP thread pool and ICP futures.
 * <p>
 * Uses invokeAll
 */
public class FutureThreadPool2 {
  private final TextFile[] tasks;
  private final ICPExecutorService executorService;


  public FutureThreadPool2(TextFile[] tasks) {
    this.tasks = tasks;
    executorService = ICPExecutors.newICPExecutorService(
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    );
  }

  Results[] compute() throws ExecutionException, InterruptedException {
    // Fork all futures and get results individually
    Results[] resultsArr = new Results[tasks.length];

    // Fork
    List<Future<Results>> futures = executorService.invokeAll(Arrays.stream(tasks)
      .map(task -> (Callable<Results>) () -> {
        Results results = new Results();
        results.word = task.word;
        results.count = WordCount.countWordsInFile(task.open(),
          task.word);
        ICP.setPermission(results, Permissions.getTransferPermission());
        return results;
      })
      .collect(Collectors.toList())
    );

    // Join
    for (int i = 0; i < futures.size(); i++) {
      resultsArr[i] = futures.get(i).get();
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

    FutureThreadPool2 app = new FutureThreadPool2(tasks);
    // Compute
    Results[] results = app.compute();

    // Print results
    for (Results result : results) {
      System.out.println("Word: " + result.word + " Count: " + result.count);
    }
  }
}
