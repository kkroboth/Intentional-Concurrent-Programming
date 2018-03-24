// $Id$

package icp.core;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Loan permission.  It is temporarily acquired by the first task that performs a check.  It can
 * be reacquired at any time by its original owner.  It is only resettable by the original owner,
 * after it has been reacquired.
 * <p>
 * <em>Permissions:</em> instances of this class are permanently thread-safe.
 */
class LoanPermission extends SingleCheckPermission {

  private final Task owner;
  private final AtomicReference<Task> borrower;

  /**
   * Primary constructor.
   */
  public LoanPermission() {
    super(true, "already acquired/reacquired");
    owner = Task.currentTask();
    borrower = new AtomicReference<>();
  }

  protected boolean singleCheck() {
    Task caller = Task.currentTask();
    Task borrower = this.borrower.get();
    // case: original owner
    if (caller == owner) {
      if (borrower != owner)
        this.borrower.set(owner);
      return true;
    }
    // case: another task
    return caller == borrower
      || borrower == null && this.borrower.compareAndSet(null, caller);
  }

  @Override
  public void checkResetPermission(Object target) {
    Task caller = Task.currentTask();
    if (caller != owner)
      throw new IntentError(String.format("task '%s' cannot reset permission on '%s' (not owner)",
        caller, ICP.identityToString(target)));
    // may do an unnecessary call to set, but shouldn't happen much (the permission is likely
    // being replaced)
    this.borrower.set(owner);
  }
}
