package icp.lib;

import icp.core.FutureTask;
import icp.core.Task;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ICP Executors.
 */
public final class ICPExecutors {

  private ICPExecutors() {
    // nope
  }

  /**
   * Creates an ICP executor service that delegates to passed in executor service.
   *
   * @param executorService executor service to delegate to
   * @return ICPExecutorService
   */
  public static ICPExecutorService newICPExecutorService(ExecutorService executorService) {
    return new Wrapper(executorService);
  }

  /**
   * Control what types of jobs can be submitted.
   * Delegates to underlying executor.
   */
  private static final class Wrapper implements ICPExecutorService {
    private final ExecutorService delegate;

    /*
     * Summary:
     *
     * Builds ICPFutureTasks and executes them in delegated executor.
     * All other methods go directly to executor.
     *
     * InvokeAll and InvokeAny go directly to underlying executor as it is more
     * complicated to build a list of future tasks from collection of callables.
     * http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/8u40-b25/java/util/concurrent/AbstractExecutorService.java
     * A CallableTask implementation COULD be used to wrap the callables before sending off
     * to executor. This is to be discussed.
     *
     * The execute(Runnable) method can be implemented in two ways:
     * 1) Don't assert the type of runnable passed in and assume it's a Task.
     * 2) Assert type is Task
     * 3) Wrap passed in runnable to a Task if it isn't already a Task
     * 4) Add another method execute(Task) and throw exception if other is used.
     *
     * This implementation does #3.
     * TODO: Should we try the other methods?
     *
     * A variant (not implemented in this class) can validate if passed in Runnables
     * are not already Tasks. Then an exception can tell the user they must pass in
     * "normal" runnable. Callables are not affected by this because there is no
     * CallableTask (correction, there is one, but may be removed).
     */

    public Wrapper(ExecutorService executorService) {
      delegate = executorService;
    }

    @Override
    public <T> FutureTask<T> submit(Callable<T> c) {
      FutureTask<T> future = new FutureTask<T>(c);
      delegate.execute(future);
      return future;
    }

    @Override
    public void shutdown() {
      delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
      return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
      return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
      return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public <T> FutureTask<T> submit(Runnable task, T result) {
      FutureTask<T> future = new FutureTask<>(task, result);
      delegate.execute(future);
      return future;
    }

    @Override
    public FutureTask<?> submit(Runnable task) {
      FutureTask<Void> future = new FutureTask<>(task, null);
      delegate.execute(future);
      return future;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
      return delegate.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
      return delegate.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
      return delegate.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return delegate.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
      // If command is a Task then use it,
      // else wrap in threadSafe task.

      // command can be a runnable that runs a Task,
      // but that is the user's fault.
      delegate.execute(command instanceof Task
        ? (Task) command
        : Task.ofThreadSafe(command));
    }
  }
}
