package icp.lib;

import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permission;
import icp.core.Permissions;
import icp.core.SingleCheckPermission;
import icp.core.TaskLocal;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * General CountDownLatch with a positive number.
 */
public final class CountDownLatch {
  private final java.util.concurrent.CountDownLatch latch;

  // Instead of dealing with AQS, CAS our count and if not zero then we can countdown the latch.
  // If zero, that's a violation. j.u.c CountDownLatch allows calling countdown below zero, we don't!
  private final AtomicInteger latchCount;

  private final AtomicInteger remainingCountDowners; // can't register more countdowners than count
  private final Permission permission;

  private final TaskLocal<Boolean> countDowners;
  private final TaskLocal<Boolean> waiters;
  private final TaskLocal<Boolean> calledCountDown;


  /**
   * Create latch with positive number count
   *
   * @param count number of times countDown is invoked before
   *              latch is opened.
   */
  public CountDownLatch(int count) {
    latch = new java.util.concurrent.CountDownLatch(count);
    latchCount = new AtomicInteger(count);
    remainingCountDowners = new AtomicInteger(count);

    countDowners = Utils.newTaskLocal(false);
    waiters = Utils.newTaskLocal(false);
    calledCountDown = Utils.newTaskLocal(false);

    permission = new SingleCheckPermission("TODO: Failed") {
      @Override
      protected boolean singleCheck() {
        // Task is countdowner
        if (countDowners.get()) {
          return latch.getCount() != 0;
        }
        // Task has to be a waiter
        else
          return waiters.get() && latch.getCount() == 0;
      }
    };

    ICP.setPermission(permission, Permissions.getFrozenPermission());
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
  }

  /**
   * Register current task as a countdowner who may call
   * <em>countDown()</em>.
   */
  public void registerCountDowner() {
    // Violation if registering more countdowners than initial count
    if (remainingCountDowners.decrementAndGet() < 0) // do we care if this value is continuous negative?
      throw new IntentError("Cannot register more countdowners than latch count");

    if (waiters.get())
      throw new IntentError("Task cannot be a waiter and countdowner");
    if (countDowners.get())
      throw new IntentError("Task already a countdowner");
    countDowners.set(true);
  }

  /**
   * Decrements latch by one. If latch reaches zero,
   * it will be opened.
   * <p>
   * <em>Violations:</em>
   * <ul>
   * <li>Calling <em>countDown()</em> after count has reached zero</li>
   * </ul>
   */
  public void countDown() {
    if (!countDowners.get())
      throw new IntentError("Task is not a countdowner");
    // Note: Don't care if a task calls countdown multiple times. As long as the count is
    // never at zero of another countdown call.
//    if (calledCountDown.get())
//      throw new IntentError("Countdowner task already called countDown()");

    int count;
    do {
      count = latchCount.get();
      if (count == 0) throw new IntentError("Latch already opened");
    } while (!latchCount.compareAndSet(count, count - 1));

    latch.countDown();
    calledCountDown.set(true);
  }

  public void registerWaiter() {
    if (countDowners.get())
      throw new IntentError("Task cannot be a waiter and countdowner");
    if (waiters.get())
      throw new IntentError("Task already a waiter");
    waiters.set(true);
  }

  /**
   * Block until the latch becomes zero.
   * <p>
   * <em>Violations:</em>
   * <ul>
   * <li>Current task called <em>countDown()</em></li>
   * </ul>
   */
  public void await() throws InterruptedException {
    if (calledCountDown.get())
      throw new IntentError("Same task cannot call countDown() then await()");
    latch.await();
  }

  public Permission getPermission() {
    return permission;
  }
}
