package application.forkjoin;

import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Provides interface for implementing fork-join applications.
 */
public class ForkJoin<T, V> {
  private final ForkJoinProvider<T, V> provider;

  public ForkJoin(ForkJoinProvider<T, V> provider) {
    this.provider = provider;
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
  }

  public void execute(Worker<T, V>[] workers) throws InterruptedException {
    JobPool<T> pool = provider.getJobPool();
    Thread[] threads = new Thread[workers.length];
    for (int i = 0; i < threads.length; i++) {
      int finalI = i;
      threads[i] = new Thread(Task.fromThreadSafeRunnable(() -> {
        // Continue to grab jobs until none exist
        T job;
        while ((job = pool.nextJob()) != null) {
          Worker<T, V> worker = workers[finalI];
          V result = worker.execute(job);
          provider.jobCompleted(result);
        }
      }), "WorkerThread-" + i);
      threads[i].start();
    }
  }

  /**
   * Create new job pool with fixed amount of work.
   *
   * @param jobs Array of jobs to be worked on
   * @param <T>  Type of Job
   * @return new job pool
   */
  public static <T> JobPool<T> fixedJobPool(T... jobs) {
    return new FixedJobPool<>(jobs);
  }

  /**
   * Keeps track of fixed number of jobs.
   */
  private static final class FixedJobPool<T> implements JobPool<T> {
    private final ConcurrentLinkedQueue<T> jobQueue;

    // use factory method
    private FixedJobPool(T... jobs) {
      jobQueue = new ConcurrentLinkedQueue<>(Arrays.asList(jobs));
      ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
    }

    public T nextJob() {
      return jobQueue.poll();
    }
  }


  public interface JobPool<T> {

    /**
     * Retrieve next job in pool or return null if none exist.
     *
     * @return next job or null
     */
    T nextJob();

  }

  public interface Worker<T, V> {

    V execute(T job);

  }

  public interface ForkJoinProvider<T, V> {

    JobPool<T> getJobPool();

    // Right now used to implement synchronizers for waiting
    void jobCompleted(V job);

  }
}
