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
   * @deprecated Use {@link #newTaskLocal(Object)}
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

  /**
   * Creates new frozen TaskLocal with supplied default value.
   *
   * @param defaultValue Initial value for task local
   * @param <T>          Type of initial value
   * @return New Tasklocal with default value
   */
  public static <T> TaskLocal<T> newTaskLocal(T defaultValue) {
    TaskLocal<T> taskLocal = new TaskLocal<T>() {
      @Override
      protected T initialValue() {
        return defaultValue;
      }
    };
    ICP.setPermission(taskLocal, Permissions.getFrozenPermission());
    return taskLocal;
  }
}
