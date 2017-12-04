package icp.lib;

import icp.core.FutureTask;
import icp.core.ICP;
import icp.core.Permission;
import icp.core.Permissions;
import icp.core.SingleCheckPermission;
import icp.core.Task;
import icp.core.TaskLocal;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

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
  private static final class Wrapper extends AbstractExecutorService implements ICPExecutorService {
    private final ExecutorService delegate;

    // WIP Await permission
    private final Permission awaitTerminationPermission;
    private final TaskLocal<Boolean> calledAwaitTermination;

    /*
     * Summary:
     *
     * Builds ICPFutureTasks and executes them in delegated executor.
     * All other methods go directly to executor.
     *
     * InvokeAll and InvokeAny will return a list of FutureTask but be of type
     * Future because List<Future> cannot be List<FutureTask
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

      calledAwaitTermination = Utils.newTaskLocal(false);
      awaitTerminationPermission = new SingleCheckPermission() {
        @Override
        protected boolean singleCheck() {
          // Task called awaitTermination and executor is terminated
          return calledAwaitTermination.get() && Wrapper.this.isTerminated();
        }

        ;
      };

      ICP.setPermission(this, Permissions.getFrozenPermission());
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
      return new FutureTask<>(runnable, value);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
      return new FutureTask<>(callable);
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
      calledAwaitTermination.set(true);
      return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public <T> FutureTask<T> submit(Runnable task, T result) {
      Objects.requireNonNull(task);
      FutureTask<T> future = (FutureTask<T>) newTaskFor(task, result);
      delegate.execute(future);
      return future;
    }

    @Override
    public FutureTask<?> submit(Runnable task) {
      Objects.requireNonNull(task);
      FutureTask<?> future = (FutureTask<?>) newTaskFor(task, null);
      delegate.execute(future);
      return future;
    }

    @Override
    public Permission getAwaitTerminationPermission() {
      return awaitTerminationPermission;
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
