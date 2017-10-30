// $Id$

package icp.lib;

import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permission;
import icp.core.Permissions;
import icp.core.SingleCheckPermission;
import icp.core.Task;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple reentrant lock, meaning it has only lock and unlock methods.
 * <p>
 * Includes a method to export its underlying permission.
 * <p>
 * It wraps a java.util.concurrent.locks.ReentrantLock.
 * <p>
 * <em>Permissions:</em> instances of this class are permanently thread-safe.
 */
public final class SimpleReentrantLock {

  private final ReentrantLock lock;
  private final Permission locked;

  /*
   * owner field is non volatile.  Uses the same trick as in java.util.concurrent:
   * - it is always written with the underlying lock help
   * - it is read in unlock without the underlying lock held.  However, a stale value could only
   *   be null or another task, which makes the test valid.
   */
  private Task owner;

  /**
   * Primary constructor. The lock is returned unlocked.
   */
  public SimpleReentrantLock() {
    lock = new ReentrantLock();
    locked = new SingleCheckPermission("lock not held") {
      protected boolean singleCheck() {
        // safer to base on task and not thread
        return Task.currentTask() == owner;
      }
    };
    ICP.setPermission(locked, Permissions.getFrozenPermission());
    // could be setting a fancier permission since owner is only read/written with the lock held
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
  }

  /**
   * Lock the lock.
   */
  public void lock() {
    lock.lock();
    if (lock.getHoldCount() == 1)
      owner = Task.currentTask();
  }

  /**
   * Unlock the lock.
   *
   * @throws IntentError if the lock is not held
   */
  public void unlock() {
    if (Task.currentTask() != owner)
      throw new IntentError("lock not held");
    if (lock.getHoldCount() == 1)
      owner = null;
    lock.unlock();
  }

  /**
   * Return the permission associated with this lock. This permission
   * can then be attached to an object to require that object to be
   * protected by this lock.  Cannot be reset.
   *
   * @return the permission associated with the lock.
   */
  public Permission getLockedPermission() {
    return locked;
  }
}