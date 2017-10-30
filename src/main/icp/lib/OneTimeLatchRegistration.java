package icp.lib;

import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permission;
import icp.core.Permissions;
import icp.core.SingleCheckPermission;
import icp.core.TaskLocal;

import java.util.concurrent.CountDownLatch;

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
public final class OneTimeLatchRegistration {
  private final CountDownLatch latch;
  private final Permission permission;

  // Allow multiple openers
  private final TaskLocal<Boolean> openers;
  private final TaskLocal<Boolean> waiters;
  private final TaskLocal<Boolean> calledAwait;

  /**
   * Initiate a one-time latch with countdown of 1.
   */
  public OneTimeLatchRegistration() {
    latch = new CountDownLatch(1);

    // TODO: Change to normal permission for more detailed
    // messages
    permission = new SingleCheckPermission("TODO: Add message why failed") {
      @Override
      protected boolean singleCheck() {
        // Valid cases:
        // 1) Waiter and latch is open
        // 2) Opener and latch is closed

        boolean isOpen = latch.getCount() == 0;

        if (openers.get())
          return !isOpen;
        else
          return waiters.get() && isOpen;
      }
    };

    // Tasks who are registered as openers
    openers = Utils.newBooleanTaskLocal(false);
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
   * <li>Task registered as waiter</li>
   * </ul>
   *
   * @throws IntentError Opener task is already an opener
   */
  public void registerOpener() {
    if (waiters.get()) {
      throw new IntentError("Cannot register tasks as opener and waiter");
    }

    if (openers.get()) {
      throw new IntentError("Already registered as opener");
    }

    openers.set(true);
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
    if (openers.get()) {
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
    if (!openers.get()) {
      throw new IntentError("Task not registered as opener");
    }

    // With multiple openers, j.u.c.CountDownLatch.countDown() does nothing
    // for counts already at zero
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
