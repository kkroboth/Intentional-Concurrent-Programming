package applications.forkjoin;

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
  private final TextFile[] textFiles;
  private final ICPExecutorService executorService;


  public FutureThreadPool2(TextFile[] textFiles) {
    this.textFiles = textFiles;
    executorService = ICPExecutors.newICPExecutorService(
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    );
  }

  List<TextFile> compute() throws InterruptedException {
    // Fork
    List<Future<TextFile>> futures = executorService.invokeAll(Arrays.stream(textFiles)
      .map(textFile -> (Callable<TextFile>) () -> {
        textFile.setCount(WordCount.countWordsInFile(textFile.open(),
          textFile.word));
        ICP.setPermission(textFile, Permissions.getTransferPermission());
        return textFile;
      })
      .collect(Collectors.toList())
    );

    executorService.shutdown();
    return futures.stream().map(f -> {
      try {
        return f.get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
        return null;
      }
    }).collect(Collectors.toList());
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

    FutureThreadPool2 app = new FutureThreadPool2(textFiles);
    // Compute
    List<TextFile> results = app.compute();

    // Print results
    for (TextFile textFile : results) {
      System.out.println("Word: " + textFile.word + " Count: " + textFile.getCount());
    }
  }
}
