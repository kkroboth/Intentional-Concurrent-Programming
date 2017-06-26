// $Id$

package icp.core;

/**
 * Core permissions.  This class can be used to create basic permissions.  Other permissions can
 * be obtained from library components (e.g., synchronizers).
 *
 * All the permissions produced by this class are permanently thread-safe.
 *
 * This class cannot be instantiated.
 */
public class Permissions {

  private Permissions() {
    throw new AssertionError("this class cannot be instantiated");
  }

  private static final Permission NO_ACCESS = new SingleCheckPermission("no access") {
    protected boolean singleCheck() {
      return false;
    }
  };

  private static final Permission PERMANENTLY_THREAD_SAFE = new SingleCheckPermission() {
    protected boolean singleCheck() {
      return true;
    }
  };

  private static final Permission THREAD_SAFE = new SingleCheckPermission(true) {
    protected boolean singleCheck() {
      return true;
    }
  };

  private static final Permission FROZEN = new FrozenPermission();

  /**
   * A permission that allows get and call, but not put or reset.
   */
  public static Permission getFrozenPermission() {
    return FROZEN;
  }

  /**
   * A permission that allows nothing.  Cannot be reset.
   */
  public static Permission getNoAccessPermission() {
    return NO_ACCESS;
  }

  /**
   * A permission that allows calls, reads and writes (e.g., "thread-safe").  Can be reset.
   */
  public static Permission getThreadSafePermission() {
    return THREAD_SAFE;
  }

  /**
   * A permission that allows calls, reads and writes but cannot be reset.  Corresponds to the
   * concept of <em>permanently thread-safe</em>.
   */
  public static Permission getPermanentlyThreadSafePermission() {
    return PERMANENTLY_THREAD_SAFE;
  }

  /**
   * A permission private to the calling task. The task that calls this method gets a permission
   * that grants all accesses (including reset) to  <em>this task</em>.  Other tasks have no access.
   * Can be reset.
   *
   * This is the default permission that is set on all new objects, unless stated otherwise.
   */
  public static Permission getPrivatePermission() {
    Task owner = Task.currentTask();
    // could be cached in a TaskLocal
    return new SingleCheckPermission(true, "private") {
      protected boolean singleCheck() {
        return Task.currentTask() == owner;
      }
    };
  }

  /**
   * A permission that transfers ownership to the first task that runs a check. Can be reset.
   *
   * The permission returned by this method is in its initial state and can be acquired by any task.
   * One acquired, it enter its final state and behaves like a private permission for the acquiring
   * task.
   */
  public static Permission getTransferPermission() {
    return new TransferPermission();
  }

  /**
   * A permission that mimics the permission of a leader object. Cannot be reset.
   */
  public static Permission getSamePermissionAs(Object leader) {
    return new SameAsPermission(leader);
  }
}