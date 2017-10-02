package application.forkjoin.synchronizers;

import application.forkjoin.ForkJoin;
import application.forkjoin.JobPool;
import application.forkjoin.WordCount;
import application.forkjoin.shared.Consumer;
import application.forkjoin.shared.Producer;
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
 * Can't use IsClosedPermission because you can only have ONE opener,
 * and there are multiple tasks producing results where any one of them
 * could be the opener. One to the CountDownLatch...
 */
public class OneTimeLatchRegistration extends WordCount {
  private final icp.lib.OneTimeLatchRegistration completeLatch;
  private final AtomicInteger jobsLeft;

  // Shared operations
  private final Consumer consumer;
  private final Producer producer;

  public OneTimeLatchRegistration(JobPool<TextFile> jobs, int count) {
    super(jobs);
    completeLatch = new icp.lib.OneTimeLatchRegistration();
    jobsLeft = new AtomicInteger(count);

    consumer = new Consumer(this); // 'this' escaped
    producer = new Producer(this);

    ICP.setPermission(producer, Permissions.getPermanentlyThreadSafePermission());
    //ICP.setPermission(producer, completeLatch.getIsClosedPermission());
    ICP.setPermission(consumer, completeLatch.getIsOpenPermission());
  }

  public List<WordResults> get() throws InterruptedException {
    completeLatch.registerWaiter();
    completeLatch.await();
    return consumer.getAllResults();
  }


  @Override
  public void jobCompleted(WordResults job) {
    producer.addResult(job);
    int left = jobsLeft.decrementAndGet();
    if (left == 0) {
      completeLatch.registerOpener();
      completeLatch.open();
    }
  }


  public static void main(String[] args) throws InterruptedException {
    JobPool jobs = ForkJoin.fixedJobPool(
      new TextFile("alice.txt", "alice"),
      new TextFile("usconst.txt", "America"),
      new TextFile("alice.txt", "the"),
      new TextFile("usconst.txt", "the"));
    OneTimeLatchRegistration forkJoin = new OneTimeLatchRegistration(jobs, 4);
    forkJoin.run(Runtime.getRuntime().availableProcessors());
    List<WordResults> results = forkJoin.get();
    System.out.println(results);
  }
}
