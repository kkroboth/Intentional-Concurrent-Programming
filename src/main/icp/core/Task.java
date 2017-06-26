// $Id$

package icp.core;

import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Top level abstract Task class.
 * Provide public access to Task.currentTask, Task.holdsLock and
 * Task.getFirstInitialTask.
 * Default access for inheritance and lower level static methods.
 */
public class Task implements Runnable {

  /**
   * The permission field.  It is set as <em>private</em> at construction time, like for any other
   * class, except for {@code InitTask}, where it is left null.
   */
  Permission icp$42$permissionField;

  private final Runnable theTask;
  private final AtomicBoolean running;

  Task() {
    theTask = null;
    running = null;
  }

    /**
     * default access constructor to only allow classes inside
       icp.lib to inherit from Task.
     */
    private Task(Runnable task) {
      if (task == null)
        throw new NullPointerException();
      theTask = task;
      running = new AtomicBoolean();
      icp$42$permissionField = Permissions.getTransferPermission();
    }

  public static Task fromPrivateRunnable(Runnable r) {
    Task task = new Task(r);
    PermissionSupport.setPermission(r, Permissions.getSamePermissionAs(task));
    return task;
  }

  public static Task fromThreadSafeRunnable(Runnable r) {
    return new Task(r);
  }

  /**
   * Runs the task.  Memory barriers ensure that a task that is re-run will see the effects
   * of its previous run.  There is no such guarantee across tasks.  It is invalid for multiple
   * threads to run a task concurrently.  Note that this enable tasks to have the same property
   * that threads have (i.e., they can see their own writes), even across multiple (but not
   * concurrent) executions by different threads.
   */
  public void run() {
    assert running != null; // only null in init task, which is not run
    if (running.getAndSet(true))
      throw new IntentError("task already running");
    Task current = CURRENT_TASK.get();
    CURRENT_TASK.set(this);
    try {
      assert theTask != null;
      // we are now a task and can check permissions
      PermissionSupport.checkCall(this);
      theTask.run();
    } finally {
      CURRENT_TASK.set(current);
      running.set(false);
    }
  }

  private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

    /**
     * Returns the current {@code Task}.
     *
     * @return long value that is the current task id.
     */
    public static Task currentTask() {
      if (!INITIALIZED.get()) {
        // bootstrapping an initial thread
        if (!INITIALIZED.getAndSet(true)) {
          Task init = new InitTask();
          assert CURRENT_TASK.get() == null; // what else could it be?
          CURRENT_TASK.set(init);
          return init;
        }
      }
      Task task = CURRENT_TASK.get();
      if (task == null)
        throw new IntentError(String.format(
            "thread '%s' is not a task",
            Thread.currentThread().getName()
        ));
      return task;
    }

    /**
     * Test if the current task holds a monitor.
     * WARNING-This method has some small potential
     * to return a false positive.
     *
     * @param object Object on which to test lock ownership.
     *
     * @return true if the current task holds the lock.
     */
    static boolean holdsLock(Object object){
      if (Thread.holdsLock(object)){
        MonitorInfo[] info = MONITOR_INFO.get();
        if (info != null){
          MonitorInfo[] currentLockedMonitors = getLockedMonitors();
          if (currentLockedMonitors.length > info.length) {

            for (MonitorInfo m : info){
              for (int i = 0; i < currentLockedMonitors.length; i++){
                if (m == currentLockedMonitors[i]) {
                  currentLockedMonitors[i] = null;
                  break;
                }
              }
            }

            MonitorInfo target = null;
            for (MonitorInfo m : currentLockedMonitors){
              if (m != null){
                target = m;
                break;
              }
            }

            return target != null &&
              object.getClass().getName().equals(target.getClassName()) &&
              System.identityHashCode(object) == target.getIdentityHashCode();
          }
        }
      }
      return false;
    }

    private static ThreadMXBean threadMXBean =
      ManagementFactory.getThreadMXBean();

    /**
     * Get all Monitors that are currently held.
     *
     * @return {@code MonitorInfo[]} of locked monitors.
     */
    protected static MonitorInfo[] getLockedMonitors(){
        long[] threadId = new long[]{Thread.currentThread().getId()};
        ThreadInfo[] threadInfo = threadMXBean.getThreadInfo(threadId, true,
          false);
        return threadInfo[0].getLockedMonitors();
    }

    /*
     * ThreadLocal for task.
     */
    public static final ThreadLocal<Task> CURRENT_TASK = new ThreadLocal<>();

    /*
     * ThreadLocal for MonitorInfo length
     */
    protected static final ThreadLocal<MonitorInfo[]> MONITOR_INFO =
      new ThreadLocal<>();

    // TaskLocal values pertaining to this task. This map is maintained
    // by the TaskLocal class.

  TaskLocal.TaskLocalMap taskLocals;
}
