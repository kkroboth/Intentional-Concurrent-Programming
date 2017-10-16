package icp.lib;

import icp.core.ICP;
import icp.core.Permission;
import icp.core.Permissions;
import icp.core.SingleCheckPermission;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

// TODO: Documentation
public class _CountDownLatch {
  private final Sync sync;
  private final Permission closed;
  private final Permission open;
  private final Permission intermediate; // Not open or closed

  private final int initialCount;

  public _CountDownLatch(int count) {
    if (count < 0) throw new IllegalArgumentException("Count is negative");
    sync = new Sync(count);
    initialCount = count;

    closed = new SingleCheckPermission("latch is open") {
      @Override
      protected boolean singleCheck() {
        return sync.getCount() > 0;
      }
    };

    open = new SingleCheckPermission("latch is closed") {
      @Override
      protected boolean singleCheck() {
        int count = sync.getCount();
        assert count >= 0; // Never below zero
        return count == 0;
      }
    };

    intermediate = new SingleCheckPermission("latch is closed or in initial count") {
      @Override
      protected boolean singleCheck() {
        int count = sync.getCount();

        assert count >= 0; // Never below zero
        return count != initialCount && count != 0;
      }
    };

    ICP.setPermission(closed, Permissions.getFrozenPermission());
    ICP.setPermission(open, Permissions.getFrozenPermission());
    ICP.setPermission(intermediate, Permissions.getFrozenPermission());
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
  }

  public void countDown() {
    sync.tryReleaseShared(0);
  }

  public void await() throws InterruptedException {
    sync.acquireSharedInterruptibly(0);
  }

  public Permission getClosedPermission() {
    return closed;
  }

  public Permission getOpenPermission() {
    return open;
  }

  public Permission getIntermediatePermission() {
    return intermediate;
  }

  private static final class Sync extends AbstractQueuedSynchronizer {

    Sync(int count) {
      setState(count);
    }

    int getCount() {
      return getState();
    }

    @Override
    protected int tryAcquireShared(int ignored) {
      return getState() == 0 ? 1 : -1;
    }

    @Override
    protected boolean tryReleaseShared(int ignored) {
      while (true) {
        int count = getState();

        // Don't care if countDown is continued to be called after reached zero
        int newCount = count - 1;
        if (newCount < 0) newCount = 0;

        if (compareAndSetState(count, newCount))
          return true; // Always the case that other release may succeed
      }
    }
  }
}
