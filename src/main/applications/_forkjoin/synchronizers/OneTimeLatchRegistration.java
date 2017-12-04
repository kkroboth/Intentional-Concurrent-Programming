package applications._forkjoin.synchronizers;

import applications._forkjoin.ForkJoin;
import applications._forkjoin.JobPool;
import applications._forkjoin.WordCount;
import applications._forkjoin.shared.SharedOperation;
import icp.core.ICP;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This example uses two synchronizers, an AtomicInteger to keep track of
 * remaining jobs and OneTimeLatch to open results to master Task.
 * <p>
 * Uses a single Data object with task-bases permissions.
 * <p>
 */
public class OneTimeLatchRegistration extends WordCount {
  private final icp.lib.OneTimeLatchRegistration completeLatch;
  private final AtomicInteger jobsLeft;

  // Shared operations
  private final SharedOperation shared;

  public OneTimeLatchRegistration(JobPool<TextFile> jobs, int count) {
    super(jobs);
    completeLatch = new icp.lib.OneTimeLatchRegistration();
    jobsLeft = new AtomicInteger(count);

    shared = new SharedOperation(this);
    ICP.setPermission(shared, completeLatch.getPermission());
  }

  public List<WordResults> get() throws InterruptedException {
    completeLatch.registerWaiter();
    completeLatch.await();
    return shared.getAllResults();
  }


  @Override
  public void jobCompleted(WordResults job) {
    completeLatch.registerOpener();
    shared.addResult(job);
    int left = jobsLeft.decrementAndGet();
    if (left == 0) {
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
