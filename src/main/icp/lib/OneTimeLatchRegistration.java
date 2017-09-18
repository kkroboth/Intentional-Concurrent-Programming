package icp.lib;

import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permission;
import icp.core.Permissions;
import icp.core.SingleCheckPermission;
import icp.core.Task;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Same as {@link OneTimeLatch} with count initialized to 1 has an additional
 * permission for latch closed.
 * <p>
 * Along with <em>isOpen</em> permission, this latch provides a <em>isClosed</em>
 * permission which grants all rights to a task registered as opener until a call
 * to {@link #open} is made.
 *
 * <em>Permissions:</em> instances of this class are permanently thread-safe.
 */
public class OneTimeLatchRegistration extends OneTimeLatch {
  private final Permission isClosed;
  private final AtomicReference<Task> openerTask;

  /**
   * Initiate a one-time latch with countdown of 1.
   */
  public OneTimeLatchRegistration() {
    openerTask = new AtomicReference<>(null);
    isClosed = new SingleCheckPermission() {
      @Override
      protected boolean singleCheck() {
        // TODO: Should we use getCount() on java.util CountDownLatch?
        return latch.getCount() != 0 && openerTask.get().equals(Task.currentTask());
      }
    };

    ICP.setPermission(isClosed, Permissions.getFrozenPermission());
  }

  /**
   * Register current task as the opener who is only allowed to call {@link #open()}
   * <p>
   *
   * <em>Violations:</em>
   * <ul>
   * <li>Task has already been set as opener (includes same task)</li>
   * <li>Task registered as waiter</li>
   * </ul>
   *
   * @throws IntentError Opener task already set or same task
   */
  public void registerOpener() {
    if (taskRegisteredAwait.get()) {
      throw new IntentError("Cannot register tasks as opener and waiter");
    }

    if (!this.openerTask.compareAndSet(null, Task.currentTask())) {
      throw new IntentError("Opener task has already been set");
    }
  }

  /**
   * Register current task as a waiter who may only call {@link #await}. Multiple unique waiter
   * tasks may be registered.
   *
   * <p>
   * <em>Violations:</em>
   * <ul>
   * <li>Same task already registered as waiter</li>
   * <li>Task registered as opener</li>
   * </ul>
   *
   * @throws IntentError task is already been registered as opener or same wait task
   */
  public void registerWaiter() {
    if (openerTask.get().equals(Task.currentTask())) {
      throw new IntentError("cannot register task as opener and waiter");
    }

    if (taskRegisteredAwait.get()) {
      throw new IntentError("Task has already been registered as waiter");
    }
    taskRegisteredAwait.set(true);
  }

  /**
   * Only the task registered as opener may call <em>open</em>
   *
   * <em>Violations:</em>
   * <ul>
   * <li>Task not registered as opener</li>
   * </ul>
   */
  @Override
  public void open() {
    if (!openerTask.get().equals(Task.currentTask())) {
      throw new IntentError("Task not registered as opener");
    }
    super.open();
  }

  /**
   * Only the task registered as a waiter may call <em>await</em>.
   * <p>
   * <em>Violations:</em>
   * <ul>
   * <li>Task not registered as waiter</li>
   * </ul>
   */
  @Override
  public void await() throws InterruptedException {
    if (!taskRegisteredAwait.get().equals(Task.currentTask())) {
      throw new IntentError("Task not registered as waiter");
    }

    latch.await();
  }

  /**
   * Return the permission associated with the latch being closed.
   *
   * <em>Violations:</em>
   * <ul>
   * <li>Either latch has been opened or task did not register as opener</li>
   * </ul>
   *
   * @return the permission associated with latch
   */
  public Permission getIsClosedPermission() {
    return isClosed;
  }
}
