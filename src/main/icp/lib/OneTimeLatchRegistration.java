package icp.lib;

import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permission;
import icp.core.Permissions;
import icp.core.SingleCheckPermission;
import icp.core.Task;
import icp.core.TaskLocal;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Same as {@link OneTimeLatch} with count initialized to 1 has an additional
 * permission for latch closed.
 * <p>
 * Uses a single task-based-permission that provides <em>isClosed</em> and
 * <em>isOpen</em> states which grant rights to a task registered under a
 * opener or waiter.
 * <p>
 * <em>Permissions:</em> instances of this class are permanently thread-safe.
 */
public class OneTimeLatchRegistration {
  private final CountDownLatch latch;
  private final Permission permission;

  private final AtomicReference<Task> openerTask;
  private final TaskLocal<Boolean> waiters;
  private final TaskLocal<Boolean> calledAwait;
  private final AtomicBoolean openCalled; // only one task can open

  /**
   * Initiate a one-time latch with countdown of 1.
   */
  public OneTimeLatchRegistration() {
    latch = new CountDownLatch(1);
    openerTask = new AtomicReference<>(null);
    openCalled = new AtomicBoolean();

    // TODO: Change to normal permission for more detailed
    // messages
    permission = new SingleCheckPermission("TODO: Add message why failed") {
      @Override
      protected boolean singleCheck() {
        // Used for close and open states
        Task curTask = Task.currentTask();

        boolean isOpen = latch.getCount() == 0;
        if (Objects.equals(openerTask.get(), curTask))
          return !isOpen;
        else
          return waiters.get() && isOpen;
      }
    };


    // Tasks who are registered as waiters
    waiters = Utils.newBooleanTaskLocal(false);
    // Tasks who called await
    calledAwait = Utils.newBooleanTaskLocal(false);

    ICP.setPermission(permission, Permissions.getFrozenPermission());
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
  }

  /**
   * Register current task as the opener who is only allowed to call {@link #open()}
   * <p>
   * <p>
   * <em>Violations:</em>
   * <ul>
   * <li>Task has already been set as opener (includes same task)</li>
   * <li>Task registered as waiter</li>
   * </ul>
   *
   * @throws IntentError Opener task already set or same task
   */
  public void registerOpener() {
    if (waiters.get()) {
      throw new IntentError("Cannot register tasks as opener and waiter");
    }

    if (!this.openerTask.compareAndSet(null, Task.currentTask())) {
      throw new IntentError("Opener task has already been set");
    }
  }

  /**
   * Register current task as a waiter who may only call {@link #await}. Multiple unique waiter
   * tasks may be registered.
   * <p>
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
    Task openerTask = this.openerTask.get();
    if (openerTask != null && openerTask.equals(Task.currentTask())) {
      throw new IntentError("cannot register task as opener and waiter");
    }

    if (waiters.get()) {
      throw new IntentError("Task has already been registered as waiter");
    }
    waiters.set(true);
  }

  /**
   * Only the task registered as opener may call <em>open</em>
   * <p>
   * <em>Violations:</em>
   * <ul>
   * <li>Task not registered as opener</li>
   * </ul>
   */
  public void open() {
    // No one can call open again (violation)
    if (!openCalled.compareAndSet(false, true)) {
      throw new IntentError("open() has been called)");
    }

    Task openerTask = this.openerTask.get();
    if (openerTask == null || !openerTask.equals(Task.currentTask())) {
      throw new IntentError("Task not registered as opener");
    }

    openCalled.set(true);
    latch.countDown();
  }

  /**
   * Only the task registered as a waiter may call <em>await</em>.
   * <p>
   * <em>Violations:</em>
   * <ul>
   * <li>Task not registered as waiter</li>
   * </ul>
   */
  public void await() throws InterruptedException {
    if (!waiters.get()) {
      throw new IntentError("Task not registered as waiter");
    }

    // Registered waiter has called await()
    calledAwait.set(true);
    latch.await();
  }

  /**
   * Return the permission associated with the latch.
   * Permission is task-based and depends on the state of latch.
   * <p>
   * <em>Closed State Violation</em>
   * <ul>
   * <li>Latch is open</li>
   * <li>Task calling open is not registered as opener</li>
   * </ul>
   * <p>
   * <em>Open State Violations</em>
   * <ul>
   * <li>Latch is closed</li>
   * <li>Task did not call await</li>
   * </ul>
   *
   * @return the permission associated with latch
   */
  public Permission getPermission() {
    return permission;
  }
}
