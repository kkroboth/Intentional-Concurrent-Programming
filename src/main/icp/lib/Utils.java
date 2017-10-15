package icp.lib;

import icp.core.External;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.TaskLocal;

/**
 * icp.lib utils.
 * <p>
 * Is @External.
 */
@External
public final class Utils {

  private Utils() {
    throw new AssertionError("Can't instantiate icp.lib.Utils");
  }

  /**
   * Creates new TaskLocal on Booleans with default supplied. Created
   * TaskLocal object has frozen permission.
   *
   * @return New TaskLocal boolean
   */
  public static TaskLocal<Boolean> newBooleanTaskLocal(boolean defaultValue) {
    TaskLocal<Boolean> taskLocal = new TaskLocal<Boolean>() {
      @Override
      protected Boolean initialValue() {
        return defaultValue;
      }
    };
    ICP.setPermission(taskLocal, Permissions.getFrozenPermission());
    return taskLocal;
  }
}
