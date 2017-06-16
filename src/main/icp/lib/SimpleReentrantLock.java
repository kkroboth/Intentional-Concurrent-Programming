// $Id$

package icp.lib;

import java.util.concurrent.locks.ReentrantLock;

import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permission;
import icp.core.FrozenPermission;

/**
 * Simple reentrant lock, meaning it has only lock and unlock methods.
 *
 * Includes a method to export its underlying permission.
 *
 * It wraps a java.util.concurrent.locks.ReentrantLock.
 */
public class SimpleReentrantLock {

  private final ReentrantLock lock;
  private Permission locked;

  // private constructor to force use of factory method
  private SimpleReentrantLock()
  {
    lock = new ReentrantLock();
  }

  /**
   *  Create and return an instance of a SimpleReentrantLock.
   *
   *  @return the created lock.
   */
  public static SimpleReentrantLock newInstance()
  {
    SimpleReentrantLock ret = new SimpleReentrantLock();
    ret.locked = SingleOwnerPermission.newInstance(ret);
    ICP.setPermission(ret, FrozenPermission.newInstance());
    return ret;
  }

  /**
   *  Lock the lock.
   */
  public void lock()
  {
    lock.lock();
  }

  /**
   *  Unlock the lock.
   */
  public void unlock()
  {
    lock.unlock();
  }

  /**
   *  Return the permission associated with this lock. This permission
   *  can then be attached to an object, to require that object to be
   *  protected by this lock.
   *
   *  @return the permission associated with the lock.
   */
  public Permission lockedPermission() {
    return locked;
  }

  private static class SingleOwnerPermission implements Permission
  {
    private final SimpleReentrantLock lock;

    private SingleOwnerPermission(SimpleReentrantLock lock)
    {
      this.lock = lock;
    }

    private static SingleOwnerPermission newInstance(SimpleReentrantLock lock)
    {
      SingleOwnerPermission ret = new SingleOwnerPermission(lock);
      ICP.setPermission(ret, FrozenPermission.newInstance());
      return ret;
    }

    @Override
    public void checkCall()
    {
      if (lock.lock.getHoldCount() < 1)
      {
        throw new IntentError("lock not held: " +lock);
      }
    }

    @Override
    public void checkGet()
    {
      if (lock.lock.getHoldCount() < 1)
      {
        throw new IntentError("lock not held: " +lock);
      }
    }

    @Override
    public void checkPut()
    {
      if (lock.lock.getHoldCount() < 1)
      {
        throw new IntentError("lock not held: " +lock);
      }
    }
  }
}
