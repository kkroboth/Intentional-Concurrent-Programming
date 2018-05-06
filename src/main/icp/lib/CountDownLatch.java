package icp.lib;

import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permission;
import icp.core.Permissions;
import icp.core.SingleCheckTaskPermission;
import icp.core.TaskLocal;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * General CountDownLatch with a positive number.
 */
public final class CountDownLatch extends DisjointTaskRegistration {
  private static final String COUNTDOWNER = "Countdowner";
  private static final String WAITER = "Waiter";

  private final java.util.concurrent.CountDownLatch latch;

  // Instead of dealing with AQS, CAS our count and if not zero then we can countdown the latch.
  // If zero, that's a violation. j.u.c CountDownLatch allows calling countdown below zero, we don't!
  private final AtomicInteger latchCount;

  private final AtomicInteger remainingCountDowners; // can't register more countdowners than count
  private final Permission permission;

  private final TaskLocal<Boolean> calledCountDown;


  /**
   * Create latch with positive number count
   *
   * @param count number of times countDown is invoked before
   *              latch is opened.
   */
  public CountDownLatch(int count) {
    super(COUNTDOWNER, WAITER);
    latch = new java.util.concurrent.CountDownLatch(count);
    latchCount = new AtomicInteger(count);
    remainingCountDowners = new AtomicInteger(count);

    calledCountDown = Utils.newTaskLocal(false);

    permission = new SingleCheckTaskPermission() {
      @Override
      protected String singleCheck() {
        // Task is countdowner
        if (isTaskRegistered(COUNTDOWNER) && latch.getCount() == 0) {
          return "Countdowner: latch is open";
        }
        // Task is a waiter
        else if (isTaskRegistered(WAITER) && latch.getCount() > 0) {
          return "Waiter: latch is closed";
        }

        return null;
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

    registerTaskIntent(COUNTDOWNER);
  }

  /**
   * Decrements latch by one. If latch reaches zero,
   * it will be opened.
   *
   * <em>Violations:</em>
   * <ul>
   * <li>Calling <em>countDown()</em> after count has reached zero</li>
   * </ul>
   */
  public void countDown() {
    checkTaskRegistered(COUNTDOWNER);
    if (calledCountDown.get())
      throw new IntentError("Countdowner task already called countDown()");

    int count;
    do {
      count = latchCount.get();
      if (count == 0) throw new IntentError("Latch already opened");
    } while (!latchCount.compareAndSet(count, count - 1));

    latch.countDown();
    calledCountDown.set(true);
  }

  public void registerWaiter() {
    registerTaskIntent(WAITER);
  }

  /**
   * Block until the latch becomes zero.
   *
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
