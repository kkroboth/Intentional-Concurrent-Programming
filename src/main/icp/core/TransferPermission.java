// $Id$

package icp.core;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Transfer permission.  It is acquired by the first task that performs a check and is resettable
 * by its owner.
 * <p>
 * <em>Permissions:</em> instances of this class are permanently thread-safe.
 */
class TransferPermission extends SingleCheckPermission {

  private final AtomicReference<Task> owner;

  /**
   * Primary constructor.
   */
  public TransferPermission() {
    super(true, "already transferred");
    owner = new AtomicReference<>();
  }

  protected boolean singleCheck() {
    Task caller = Task.currentTask();
    Task owner = this.owner.get();
    return caller == owner
      || owner == null && this.owner.compareAndSet(null, caller);
  }
}
