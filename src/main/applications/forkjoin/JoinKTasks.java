package applications.forkjoin;

import applications.forkjoin.shared.TextFile;
import applications.forkjoin.shared.WordCount;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;

import java.util.Arrays;

/**
 * Join K textFiles for K files.
 * Uses join-permission on all textFiles.
 */
public class JoinKTasks {
  // Permissions[index]: Join Permission
  private final TextFile[] textFiles;

  private final Task[] threadTasks;

  JoinKTasks(TextFile[] textFiles) {
    this.textFiles = textFiles;
    this.threadTasks = new Task[textFiles.length];
  }

  void compute() {
    for (int i = 0; i < textFiles.length; i++) {
      int finalI = i;

      threadTasks[i] = Task.ofThreadSafe(() -> {
        TextFile textFile = textFiles[finalI];
        textFile.setCount(WordCount.countWordsInFile(textFile.open(),
          textFile.word));
      });

      // Add the join permission on each array element
      ICP.setPermission(textFiles[i], threadTasks[i].getJoinPermission());

      new Thread(threadTasks[i]).start();
    }
  }


  void awaitComputation() throws InterruptedException {
    for (Task threadTask : threadTasks) {
      threadTask.join();
    }

  }

  public static void main(String[] args) throws InterruptedException {
    TextFile[] textFiles = new TextFile[]{
      new TextFile("alice.txt", "the"),
      new TextFile("alice.txt", "alice"),
      new TextFile("alice.txt", "I"),
    };

    // Transfer the array of text files
    Arrays.stream(textFiles).forEach(t -> ICP.setPermission(t, Permissions.getTransferPermission()));

    JoinKTasks app = new JoinKTasks(textFiles);
    // Compute
    app.compute();
    app.awaitComputation();

    // Print results
    for (TextFile textFile : textFiles) {
      System.out.println("Word: " + textFile.word + " Count: " + textFile.getCount());
    }
  }
}
