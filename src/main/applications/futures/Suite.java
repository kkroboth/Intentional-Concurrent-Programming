package applications.futures;


import applications.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base code for creating and managing executors.
 */
public abstract class Suite {
  // Same object, two different interfaces
  protected final ExecutorService executorService;

  /**
   * Set Executor interfaces
   *
   * @param executorService Executor
   */
  public Suite(ExecutorService executorService) {
    this.executorService = executorService;
  }

  /**
   * Sets executor to cached thread pool.
   */
  public Suite() {
    this(Executors.newCachedThreadPool(Utils.logExceptionThreadFactory()));
  }

  public final void shutdown() {
    executorService.shutdown();
  }
}
