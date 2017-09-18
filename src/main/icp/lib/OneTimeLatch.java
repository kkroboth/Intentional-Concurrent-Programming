package icp.lib;

import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permission;
import icp.core.Permissions;
import icp.core.SingleCheckPermission;
import icp.core.TaskLocal;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CountDownLatch initialized with 1.
 * <p>
 * Only one task can call {@link #open}. This tasks cannot call await.
 * <p>
 * The latch provides an <em>isOpen</em> permission which grants all rights to tasks
 * that have completed a call to {@link #await}.
 * <p>
 * <em>Permissions:</em> instances of this class are permanently thread-safe.
 */
public class OneTimeLatch {
  private final CountDownLatch latch;
  private final Permission isOpen;
  private final AtomicBoolean openCalled; // only one task can open

  /**
   * Initiate one-time latch (countdownlatch of 1).
   */
  public OneTimeLatch() {
    latch = new CountDownLatch(1);
    openCalled = new AtomicBoolean();

    isOpen = new SingleCheckPermission("latch not open") {
      @Override
      protected boolean singleCheck() {
        // No need to check latch -- if one called await then it must be opened on return
        return taskRegisteredAwait.get();
      }
    };

    ICP.setPermission(isOpen, Permissions.getFrozenPermission());
    ICP.setPermission(taskCalledOpen, Permissions.getFrozenPermission());
    ICP.setPermission(taskRegisteredAwait, Permissions.getFrozenPermission());
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
  }

  /**
   * Open the latch and allow tasks who called {@link #await} to unblock.
   * Once latch is opened, it cannot not be opened again.
   * The task who called <em>open</em> may not call <em>await</em>.
   * <p>
   *
   * <em>Violations:</em>
   * <ul>
   * <li>Same task calling <em>await</em> after <em>open</em></li>
   * <li>More than one task calling <em>open</em></li>
   * </ul>
   *
   * @throws IntentError if call to <em>open</em> has already been called
   */
  public void open() {
    // No one can call open again (violation)
    if (!openCalled.compareAndSet(false, true)) {
      throw new IntentError("open() has been called)");
    }

    // Current task is not allowed to call await
    taskCalledOpen.set(true);
    latch.countDown();
  }


  /**
   * The current task will wait until the latch is opened. Once a call to <em>await</em>
   * is made, the same task cannot call {@link #open}.
   * <p>
   *
   * <em>Violations:</em>
   * <ul>
   * <li>Same Task calling <em>open</em> after <em>await</em></li>
   * </ul>
   *
   * <em>Permissions:</em>
   * <ul>
   * <li><em>IsOpen</em> grants all rights to tasks that have completed a call to <em>await</em></li>
   * </ul>
   *
   * @throws InterruptedException if current thread is interrupted while waiting
   */
  public void await() throws InterruptedException {
    // Task who called open cannot call await (violation)
    if (taskCalledOpen.get()) {
      throw new IntentError("Same task called open()");
    }

    // Register task in wait pool
    taskRegisteredAwait.set(true);

    latch.await();
  }

  /**
   * Return the permission associated with the latch being opened.
   *
   * <em>Violations:</em>
   * <ul>
   * <li>Task has not called <em>await</em></li>
   * </ul>
   *
   * @return the permission associated with latch.
   */
  public Permission getIsOpenPermission() {
    return isOpen;
  }

  // Set flag to true once a Task calls open. That same task cannot call await.
  private static final TaskLocal<Boolean> taskCalledOpen = new CalledOpenTaskLocal();

  private static class CalledOpenTaskLocal extends TaskLocal<Boolean> {
    @Override
    protected Boolean initialValue() {
      return Boolean.FALSE;
    }
  }

  // await task registration
  private static final TaskLocal<Boolean> taskRegisteredAwait = new RegisterAwaitTaskLocal();

  private static class RegisterAwaitTaskLocal extends TaskLocal<Boolean> {
    @Override
    protected Boolean initialValue() {
      return Boolean.FALSE;
    }
  }
}
