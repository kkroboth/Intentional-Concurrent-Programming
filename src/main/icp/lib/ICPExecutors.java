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
import java.util.concurrent.Future;
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
    return new ICPExecutorAdapter(executorService);
  }

  private static final class ICPExecutorAdapter extends AbstractExecutorService
    implements ICPExecutorService {
    private final ExecutorService delegate;

    private final Permission awaitTerminationPermission;
    private final TaskLocal<Boolean> calledAwaitTermination;

    /*
     * Summary:
     *
     * Builds ICPFutureTasks and executes them in delegated executor.
     * All other methods go directly to executor.
     */

    public ICPExecutorAdapter(ExecutorService executorService) {
      delegate = executorService;

      calledAwaitTermination = Utils.newTaskLocal(false);
      awaitTerminationPermission = new SingleCheckPermission() {
        @Override
        protected boolean singleCheck() {
          // Task called awaitTermination and executor is terminated
          return calledAwaitTermination.get() && ICPExecutorAdapter.this.isTerminated();
        }
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
    public <T> Future<T> submit(Task task, T result) {
      return submit((Runnable) task, result);
    }

    @Override
    public Future<?> submit(Task task) {
      return submit((Runnable) task);
    }

    @Override
    public Permission getAwaitTerminationPermission() {
      return awaitTerminationPermission;
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
    public void execute(Task command) {
      delegate.execute(command);
    }

    @Override
    public void execute(Runnable command) {
      delegate.execute(command);
    }
  }
}
