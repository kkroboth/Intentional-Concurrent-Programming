package applications.forkjoin;

import applications.forkjoin.shared.Results;
import applications.forkjoin.shared.TextFile;
import applications.forkjoin.shared.WordCount;
import icp.core.ICP;
import icp.core.Task;

/**
 * Join K tasks for K files.
 * Uses join-permission on all tasks.
 */
public class JoinKTasks {
  private final TextFile[] tasks;

  // Permission[index]: Join Permission
  private final Results[] results;
  private final Task[] threadTasks;

  JoinKTasks(TextFile[] textFiles) {
    this.tasks = textFiles;
    this.results = new Results[textFiles.length];
    this.threadTasks = new Task[textFiles.length];
  }

  void compute() {
    for (int i = 0; i < tasks.length; i++) {
      int finalI = i;

      results[i] = new Results();
      threadTasks[i] = Task.ofThreadSafe(() -> {
        results[finalI].word = tasks[finalI].word;
        results[finalI].count = WordCount.countWordsInFile(tasks[finalI].open(),
          tasks[finalI].word);
      });

      // Add the join permission on each array element
      ICP.setPermission(results[i], threadTasks[i].getJoinPermission());

      new Thread(threadTasks[i]).start();
    }
  }


  Results[] getResults() throws InterruptedException {
    for (Task threadTask : threadTasks) {
      threadTask.join();
    }

    return results;
  }

  public static void main(String[] args) throws InterruptedException {
    TextFile[] tasks = new TextFile[]{
      new TextFile("alice.txt", "the"),
      new TextFile("alice.txt", "alice"),
      new TextFile("alice.txt", "I"),
    };

    JoinKTasks app = new JoinKTasks(tasks);
    // Compute
    app.compute();

    Results[] results = app.getResults();
    // Print results
    for (Results result : results) {
      System.out.println("Word: " + result.word + " Count: " + result.count);
    }
  }
}
