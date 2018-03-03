package icp.lib;

import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permissions;
import icp.core.TaskLocal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Disjoint set of acquirers and releasers semaphore.
 */
public class DisjointSemaphore {

  /*
   * Overview:
   *
   * This synchronizer only catches misuse of the semaphore and does not guard
   * objects using permissions. This may change in the future through another class.
   *
   * Acquirers and releasers are disjoint and intent errors are thrown if one tries
   * to register as both.
   *
   * Virtual permit objects (interface to public) are given to users and are associated
   * with acquire() and release() methods. Only releasers may release a permit.
   *
   * Only this class may create permits (VirtualPermit) and it can be assumed when
   * release() is called, that permit was associated with this class. Never the case
   * release() is called without acquire() beforehand.
   */

  // j.u.c semaphore
  private final Semaphore semaphore;

  // Register acquirers and releasers
  private final TaskLocal<Boolean> acquirers, releasers;

  public DisjointSemaphore(int permits) {
    this(permits, false);
  }

  public DisjointSemaphore(int permits, boolean fair) {
    semaphore = new Semaphore(permits, fair);
    acquirers = Utils.newTaskLocal(false);
    releasers = Utils.newTaskLocal(false);


    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
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

  public Permit acquire() throws InterruptedException {
    return acquire(1).get(0);
  }

  public List<Permit> acquire(int permits) throws InterruptedException {
    if (!acquirers.get())
      throw new IntentError("Task not a acquirer");
    semaphore.acquire(permits);
    List<Permit> virtualPermits = new ArrayList<>(permits);
    for (int i = 0; i < permits; i++) {
      virtualPermits.add(new VirtualPermit());
    }

    return Collections.unmodifiableList(virtualPermits);
  }

  public void release(List<Permit> permits) {
    if (!releasers.get())
      throw new IntentError("Task not releaser");

    for (Permit permit : permits) {
      if (!(permit instanceof DisjointSemaphore.VirtualPermit))
        throw new IntentError("Permit object not associated with DisjointSemaphore");
      if (!releasers.get()) {
        throw new IntentError("Current task is not a releaser");
      }
    }

    semaphore.release(permits.size());
  }

  public void release(Permit permit) {
    release(Collections.singletonList(permit));
  }

//  /**
//   * Get permission associated with semaphore that requires a Task acquiring
//   * <code>requiredPermits</code> or more.
//   *
//   * @param requiredPermits lower bound of permits required
//   * @return acquired permission
//   */
//  public Permission getAcquiredPermission(int requiredPermits) {
//    Permission permission = new SingleCheckPermission() {
//      @Override
//      protected boolean singleCheck() {
//        return acquiredPermits.get() >= requiredPermits;
//      }
//    };
//
//    ICP.setPermission(permission, Permissions.getFrozenPermission());
//    return permission;
//  }

  /**
   * The permit users use to acquire and release.
   */
  private final class VirtualPermit implements Permit {
  }
}
