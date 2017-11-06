package icp.core;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @param <V> The result type of get methods.
 */
public class FutureTask<V> extends Task implements RunnableFuture<V> {

  // TODO: Do we want private permissions here?
  // Multiple threads could call other methods than get()?

  /*
   * Overview:
   *
   * With the current ICP system, either an extended Task has a runnable passed in
   * constructor or null. A null runnable is never ran which is what InitTask
   * (already running) does. FutureTask is similar of InitTask except it does run.
   *
   * A DUMMY runnable is used as this task must be ran (unlike InitTask), but the
   * task's runnable is never used. A static factory method could be implemented,
   * but there isn't a reason why this class can't be extended.
   *
   * Since the doRun() method in Task is overriden, no wrapping of Runnable and
   * Callables interfaces are required.
   *
   * icp.core package classes are not edited, so manual permission checking is required.
   * Fortunately, there are no public fields (no get/put checks).
   */

  private final static Runnable DUMMY = () -> {
  };

  // This FutureTask allows setting of permissions,
  // but it must reside in icp.core (ignored) package to extend Task
  // TODO: Maybe clean up this mess of manual permission checks in every method?
  @SuppressWarnings("unused")
  private Permission icp$42$permissionField;

  private final java.util.concurrent.FutureTask<V> underlyingFutureTask;

  public FutureTask(Callable<V> callable) {
    // Can't create FutureTask before passing to super()
    super(DUMMY);
    // always private to task that created future by default
    icp$42$permissionField = Permissions.getPrivatePermission();
    underlyingFutureTask = new java.util.concurrent.FutureTask<>(callable);
  }

  public FutureTask(Runnable runnable, V result) {
    this(Executors.callable(runnable, result));
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    PermissionSupport.checkCall(this);
    return underlyingFutureTask.cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    PermissionSupport.checkCall(this);
    return underlyingFutureTask.isCancelled();
  }

  @Override
  public boolean isDone() {
    PermissionSupport.checkCall(this);
    return underlyingFutureTask.isDone();
  }

  @Override
  public V get() throws InterruptedException, ExecutionException {
    PermissionSupport.checkCall(this);
    return underlyingFutureTask.get();
  }

  @Override
  public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    PermissionSupport.checkCall(this);
    return underlyingFutureTask.get(timeout, unit);
  }

  @Override
  void doRun() {
    // No permission check -- this is overridden run functionality in Task
    underlyingFutureTask.run();
  }
}
