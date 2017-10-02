package icp.lib;

import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permission;
import icp.core.Permissions;
import icp.core.SingleCheckPermission;
import icp.core.TaskLocal;

import java.util.concurrent.CountDownLatch;

public class CountDownLatchRegistration {
  private final CountDownLatch latch;
  private final Permission isClosed;
  private final Permission isOpen;

  private final TaskLocal<Boolean> taskCalledOpen = new TaskLocal<Boolean>() {
    @Override
    protected Boolean initialValue() {
      return Boolean.FALSE;
    }
  };

  private final TaskLocal<Boolean> taskRegisteredAwait = new TaskLocal<Boolean>() {
    @Override
    protected Boolean initialValue() {
      return Boolean.FALSE;
    }
  };

  public CountDownLatchRegistration(int count) {
    latch = new CountDownLatch(count);

    isOpen = new SingleCheckPermission(true, "latch is open") {
      @Override
      protected boolean singleCheck() {
        return taskRegisteredAwait.get();
      }
    };

    isClosed = new SingleCheckPermission(true, "latch is closed") {
      @Override
      protected boolean singleCheck() {
        // TODO: Finish
        return false;
      }
    };

    ICP.setPermission(isOpen, Permissions.getFrozenPermission());
    ICP.setPermission(isClosed, Permissions.getFrozenPermission());
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
  }

  public void countDown() {
    // TODO: Race condition on this if-then-action of
    // check count then countdown.
    if (latch.getCount() == 0) {
      throw new IntentError("latch has already been opened");
    }

    // Current task is now allowed to call await
    taskCalledOpen.set(true);
    latch.countDown();
  }

  public void await() throws InterruptedException {
    // Task who called countdown cannot call await (violation)
    if (taskCalledOpen.get()) {
      throw new IntentError("Same task called countDown()");
    }

    taskRegisteredAwait.set(true);
    latch.await();
  }

  public Permission getIsOpenPermission() {
    return isOpen;
  }

  public Permission getIsClosedPermission() {
    return isClosed;
  }
}
