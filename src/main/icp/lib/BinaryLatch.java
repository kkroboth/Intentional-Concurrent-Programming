package icp.lib;


import icp.core.ICP;
import icp.core.Permission;
import icp.core.Permissions;
import icp.core.SingleCheckPermission;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * One time binary latch.
 * <p>
 * It wraps {@link java.util.concurrent.CountDownLatch}.
 */
public class BinaryLatch {
  private final Sync sync;
  private final Permission closed;
  private final Permission open;


  /**
   * Initialize Binary Latch
   */
  public BinaryLatch() {
    sync = new Sync();

    closed = new SingleCheckPermission("latch is closed") {
      @Override
      protected boolean singleCheck() {
        return !sync.isOpen();
      }
    };

    open = new SingleCheckPermission("latch is opened") {
      @Override
      protected boolean singleCheck() {
        return sync.isOpen();
      }
    };

    ICP.setPermission(closed, Permissions.getFrozenPermission());
    ICP.setPermission(open, Permissions.getFrozenPermission());
    ICP.setPermission(sync, Permissions.getFrozenPermission()); // Is this correct?
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
  }

  public void countDown() {
    sync.releaseShared(0);
  }

  public void await() throws InterruptedException {
    sync.acquireSharedInterruptibly(0);
  }

  /**
   * Return the closed latch permission associated with this latch. This permission
   * can then be attached to an object to require that object to be
   * protected by this lock.  Cannot be reset.
   *
   * @return the closed permission associated with the latch.
   */
  public Permission getClosedPermission() {
    return closed;
  }

  /**
   * Return the open latch permission associated with this latch. This permission
   * can then be attached to an object to require that object to be
   * protected by this lock.  Cannot be reset.
   *
   * @return the open permission associated with the latch.
   */
  public Permission getOpenPermission() {
    return open;
  }


  private static final class Sync extends AbstractQueuedSynchronizer {


    @Override
    protected int tryAcquireShared(int ignored) {
      return getState() == 1 ? 1 : -1;
    }

    @Override
    protected boolean tryReleaseShared(int ignored) {
      setState(1);
      return true;
    }

    boolean isOpen() {
      return getState() == 1;
    }
  }
}
