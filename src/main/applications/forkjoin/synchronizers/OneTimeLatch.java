package applications.forkjoin.synchronizers;

import applications.forkjoin.ForkJoin;
import applications.forkjoin.JobPool;
import applications.forkjoin.WordCount;
import applications.forkjoin.shared.Consumer;
import applications.forkjoin.shared.Producer;
import icp.core.ICP;
import icp.core.Permissions;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This example uses two synchronizers, an AtomicInteger to keep track of
 * remaining jobs and OneTimeLatch to open results to master Task.
 * <p>
 * Uses a Consumer and Producer objects. Only consumer has a
 * isOpenPermission attached.
 *
 * <p>
 * <em>Problems:</em>
 * The producer must be ThreadSafe and is allowed to continue add more results
 * after the latch has opened. OneTimeLatchRegistration solves that issue with
 * a isClosed permission.
 */
public class OneTimeLatch extends WordCount {
  private final icp.lib.OneTimeLatch completeLatch;
  private final AtomicInteger jobsLeft;

  // Shared operations
  private final Consumer consumer;
  private final Producer producer;

  public OneTimeLatch(JobPool<WordCount.TextFile> jobs, int count) {
    super(jobs);
    completeLatch = new icp.lib.OneTimeLatch();
    jobsLeft = new AtomicInteger(count);

    consumer = new Consumer(this);
    producer = new Producer(this);

    ICP.setPermission(producer, Permissions.getPermanentlyThreadSafePermission());
    ICP.setPermission(consumer, completeLatch.getIsOpenPermission());
  }

  public List<WordResults> get() throws InterruptedException {
    completeLatch.await();
    return consumer.getAllResults();
  }


  @Override
  public void jobCompleted(WordResults job) {
    producer.addResult(job);
    int left = jobsLeft.decrementAndGet();
    if (left == 0) completeLatch.open();
  }


  public static void main(String[] args) throws InterruptedException {
    JobPool jobs = ForkJoin.fixedJobPool(
      new TextFile("alice.txt", "alice"),
      new TextFile("usconst.txt", "America"),
      new TextFile("alice.txt", "the"),
      new TextFile("usconst.txt", "the"));
    OneTimeLatch forkJoin = new OneTimeLatch(jobs, 4);
    forkJoin.run(Runtime.getRuntime().availableProcessors());
    List<WordResults> results = forkJoin.get();
    System.out.println(results);
  }
}
