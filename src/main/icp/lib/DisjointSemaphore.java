package icp.lib;

import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permission;
import icp.core.Permissions;
import icp.core.SingleCheckPermission;
import icp.core.TaskLocal;

import java.util.concurrent.Semaphore;

/**
 * Disjoint set of acquirers and releasers semaphore.
 */
public class DisjointSemaphore {
  // j.u.c semaphore
  private final Semaphore semaphore;

  // Register acquirers and releasers
  private final TaskLocal<Boolean> acquirers, releasers;
  private final TaskLocal<Integer> acquiredPermits;

  public DisjointSemaphore(int permits) {
    this(permits, false);
  }

  public DisjointSemaphore(int permits, boolean fair) {
    semaphore = new Semaphore(permits, fair);
    acquirers = Utils.newBooleanTaskLocal(false);
    releasers = Utils.newBooleanTaskLocal(false);
    acquiredPermits = Utils.newTaskLocal(0);


    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
    ICP.setPermission(permits, Permissions.getFrozenPermission());
  }

  public void registerAcquirer() {
    if (acquirers.get()) {
      throw new IntentError("Task already registered as Acquirer");
    }
    if (releasers.get()) {
      throw new IntentError("Cannot register task as acquirer and releaser");
    }

    acquirers.set(true);
  }

  public void registerReleaser() {
    if (releasers.get()) {
      throw new IntentError("Task already registered as Releaser");
    }
    if (acquirers.get()) {
      throw new IntentError("Cannot register task as acquirer and releaser");
    }

    releasers.set(true);
  }

  public void acquire() throws InterruptedException {
    acquire(1);
  }

  public void acquire(int permits) throws InterruptedException {
    if (!acquirers.get())
      throw new IntentError("Task not a acqurier");
    semaphore.acquire(permits);
  }

  public void release(int permits) {
    if (!releasers.get())
      throw new IntentError("Task not releaser");
    semaphore.release(permits);
    acquiredPermits.set(acquiredPermits.get() - permits);
  }

  public void release() {
    release(1);
  }

  /**
   * Get permission associated with semaphore that requires a Task acquiring
   * <code>requiredPermits</code> or more.
   *
   * @param requiredPermits lower bound of permits required
   * @return acquired permission
   */
  public Permission getAcquiredPermission(int requiredPermits) {
    Permission permission = new SingleCheckPermission() {
      @Override
      protected boolean singleCheck() {
        return acquiredPermits.get() >= requiredPermits;
      }
    };

    ICP.setPermission(permission, Permissions.getFrozenPermission());
    return permission;
  }
}
