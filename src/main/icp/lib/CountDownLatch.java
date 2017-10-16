package icp.lib;

import icp.core.External;
import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permission;
import icp.core.Permissions;
import icp.core.SingleCheckPermission;
import icp.core.TaskLocal;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * General CountDownLatch with a positive number.
 */
public class CountDownLatch {
  private final Sync sync;
  private final AtomicInteger remainingCountDowners;
  private final Permission permission;
  private final TaskLocal<Boolean> countDowners;
  private final TaskLocal<Boolean> waiters;


  /**
   * Create latch with positive number count
   *
   * @param count number of times countDown is invoked before
   *              latch is opened.
   */
  public CountDownLatch(int count) {
    if (count <= 0) throw new IllegalArgumentException("Count must be a positive number");
    remainingCountDowners = new AtomicInteger(count);
    sync = new Sync(count);
    countDowners = Utils.newBooleanTaskLocal(false);
    waiters = Utils.newBooleanTaskLocal(false);

    permission = new SingleCheckPermission("TODO: Failed") {
      @Override
      protected boolean singleCheck() {
        // Task is countdowner
        if (countDowners.get()) {
          return !sync.calledCountDown();
        }
        // Task has to be a waiter
        else
          return waiters.get() && sync.getCount() == 0;
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
    sync.releaseShared(1);
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
    sync.acquireSharedInterruptibly(1);
  }

  public Permission getPermission() {
    return permission;
  }

  // AQS for countdown latch
  @External
  private static final class Sync extends AbstractQueuedSynchronizer {

    private final TaskLocal<Boolean> calledCountDown;

    Sync(int count) {
      assert count > 0;
      calledCountDown = Utils.newBooleanTaskLocal(false);
      setState(count);
    }

    /**
     * Outside access of state.
     */
    int getCount() {
      return getState();
    }

    boolean calledCountDown() {
      return calledCountDown.get();
    }

    @Override
    protected int tryAcquireShared(int arg) {
      // Await:
      if (calledCountDown.get()) throw new IntentError("Same task cannot call countDown() then await()");
      return getState() == 0 ? 1 : -1;
    }

    @Override
    protected boolean tryReleaseShared(int arg) {
      // CountDown:
      if (calledCountDown.get())
        throw new IntentError("Countdowner task already called countDown()");
      int count;
      boolean nowOpen = false;
      do {
        count = getState();
        if (count == 0)
          throw new IntentError("CountdownLatch already opened");
        else if (count - 1 == 0) nowOpen = true;
      } while (!compareAndSetState(count, count - 1));

      calledCountDown.set(true);
      return nowOpen;
    }
  }

}
