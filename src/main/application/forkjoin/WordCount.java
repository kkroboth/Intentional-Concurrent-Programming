package application.forkjoin;

import icp.core.ICP;
import icp.core.Permissions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class WordCount implements ForkJoinProvider<WordCount.TextFile,
  WordCount.WordResults> {

  private final JobPool<TextFile> jobs;
  private final ForkJoin<TextFile, WordResults> executor;
  private final ConcurrentLinkedQueue<WordResults> results;

  public WordCount(JobPool<TextFile> jobs) {
    this.jobs = jobs;
    executor = new ForkJoin<>(this); // 'this' escapes
    results = new ConcurrentLinkedQueue<>();
    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  public void run(int nbWorkers) throws InterruptedException {
    Worker<TextFile, WordResults>[] workers = new Worker[nbWorkers];
    for (int i = 0; i < nbWorkers; i++) {
      workers[i] = new ThreadTask();
    }

    executor.execute(workers);
  }

  public final void addResult(WordResults results) {
    this.results.add(results);
  }

  public final List<WordResults> getResults() {
    return new ArrayList<>(results);
  }

  @Override
  public final JobPool<TextFile> getJobPool() {
    return jobs;
  }

  // Text file in resources folder
  public static class TextFile {
    final String name;
    final String word;

    public TextFile(String name, String word) {
      this.name = name;
      this.word = word;
      ICP.setPermission(this, Permissions.getFrozenPermission());
    }

    public BufferedReader open() {
      return new BufferedReader(new InputStreamReader(getClass().getClassLoader()
        .getResourceAsStream(name)));
    }

  }

  // Holds results of counted words
  public static class WordResults {
    final String word;
    final int count;

    public WordResults(String word, int count) {
      this.word = word;
      this.count = count;
      ICP.setPermission(this, Permissions.getFrozenPermission());
    }

    @Override
    public String toString() {
      return "Word: '" + word + "' Count: " + count;
    }
  }

  static class ThreadTask extends Thread implements Worker<TextFile, WordResults> {

    ThreadTask() {
      ICP.setPermission(this, Permissions.getFrozenPermission());
    }

    @Override
    public WordResults execute(TextFile job) {
      try (BufferedReader reader = job.open()) {
        String line;
        int count = 0;
        while ((line = reader.readLine()) != null) {
          String[] words = line.split("\\s+");
          for (String word : words) {
            if (word.equalsIgnoreCase(job.word)) count++;
          }
        }

        return new WordResults(job.word, count);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
