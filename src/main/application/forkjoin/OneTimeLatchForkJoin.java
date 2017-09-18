package application.forkjoin;

import icp.core.ICP;
import icp.core.Permissions;
import icp.lib.OneTimeLatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO: I (Kyle) will fix this mess
 * TODO: Does not use any permissions at the moment -- only crude setup
 */
public class OneTimeLatchForkJoin implements ForkJoin.ForkJoinProvider<OneTimeLatchForkJoin.TextFile,
  OneTimeLatchForkJoin.WordResults> {
  final ForkJoin.JobPool<TextFile> jobs;
  final ForkJoin<TextFile, WordResults> executor;
  final OneTimeLatch completeLatch;
  final AtomicInteger jobsLeft;

  final ConcurrentLinkedQueue<WordResults> results;

  public OneTimeLatchForkJoin() {
    jobs = ForkJoin.fixedJobPool(new TextFile("alice.txt", "alice"),
      new TextFile("usconst.txt", "America"), new TextFile("alice.txt", "the"),
      new TextFile("usconst.txt", "the"));
    executor = new ForkJoin<>(this); // 'this' escapes
    completeLatch = new OneTimeLatch();
    jobsLeft = new AtomicInteger(4);
    results = new ConcurrentLinkedQueue<>();
    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  public void run(int nbWorkers) throws InterruptedException {
    ForkJoin.Worker<TextFile, WordResults>[] workers = new ForkJoin.Worker[nbWorkers];
    for (int i = 0; i < nbWorkers; i++) {
      workers[i] = new ThreadTask();
    }

    executor.execute(workers);
  }

  public List<WordResults> get() throws InterruptedException {
    completeLatch.await();
    return new ArrayList<>(results);
  }

  @Override
  public ForkJoin.JobPool<TextFile> getJobPool() {
    return jobs;
  }

  @Override
  public void jobCompleted(WordResults job) {
    results.add(job);
    int left = jobsLeft.decrementAndGet();
    if (left == 0) completeLatch.open();
  }


  // Text file in resources folder
  static class TextFile {
    final String name;
    final String word;

    TextFile(String name, String word) {
      this.name = name;
      this.word = word;
      ICP.setPermission(this, Permissions.getFrozenPermission());
    }

    BufferedReader open() {
      return new BufferedReader(new InputStreamReader(getClass().getClassLoader()
        .getResourceAsStream(name)));
    }

  }

  // Holds results of counted words
  static class WordResults {
    final String word;
    final int count;

    WordResults(String word, int count) {
      this.word = word;
      this.count = count;
      ICP.setPermission(this, Permissions.getFrozenPermission());
    }

    @Override
    public String toString() {
      return "Word: '" + word + "' Count: " + count;
    }
  }

  static class ThreadTask extends Thread implements ForkJoin.Worker<TextFile, WordResults> {

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

  public static void main(String[] args) throws InterruptedException {
    OneTimeLatchForkJoin forkJoin = new OneTimeLatchForkJoin();
    forkJoin.run(Runtime.getRuntime().availableProcessors());
    List<WordResults> results = forkJoin.get();
    System.out.println(results);
  }
}
