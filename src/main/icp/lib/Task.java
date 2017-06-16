// $Id$

package icp.lib;

import icp.core.IntentError;
import icp.core.ICP;

import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * Top level abstract Task class.
 * Provide public access to Task.currentTask, Task.holdsLock and
 * Task.getFirstInitialTask.
 * Default access for inheritance and lower level static methods.
 */
abstract public class Task {

    /**
     * default access constructor to only allow classes inside
       icp.lib to inherit from Task.
     */
    Task(){}

    /**
     * Returns the current {@code Task}.
     *
     * @return long value that is the current task id.
     */
    public static Task currentTask(){
      Task toRtn = CURRENT_TASK.get();
      // what if no current task established for this thread?
      if (toRtn == null) {
        // if the current thread is an instance of icp.lib.Thread then
        // there is an internal error.
        java.lang.Thread thread = java.lang.Thread.currentThread();
        assert(!(thread instanceof icp.lib.Thread));

        // otherwise, this should be the "main" thread, which is okay
        // and we give it an initial task.
        // but it is possible that the user has created a non-icp.lib.Thread,
        // which we want to treat as an error.
        // to catch this, we will only allow one non-icp.lib.Thread to be
        // given an initial task.
        // there could be a race between two non-ICP threads so lock on
        // the Task class object
        synchronized(Task.class)
        {
          if (ICP.mainThreadBootstrapped())
          {
            throw new IntentError(
              "not the main thread and not an instance of icp.lib.Thread");
          }

          // okay, go ahead and give the main thread a task
          ICP.announceMainThreadBootstrapped();
          toRtn = Task.getFirstInitialTask();
          CURRENT_TASK.set(toRtn);
        }
      }
      return toRtn;
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

    public static Task getFirstInitialTask(){
        return new InitialTask();
    }

    /*
     * ThreadLocal for MonitorInfo length
     */
    protected static final ThreadLocal<MonitorInfo[]> MONITOR_INFO =
      new ThreadLocal<>();

    // TaskLocal values pertaining to this task. This map is maintained
    // by the TaskLocal class.

    // This is tricky, null is explicitly assigned. This is redundant, as
    // the JVM will guarantee the field will be null, however this is the
    // only hook to get the checkPutField method of InitialTaskPermission
    // to be called. Without explicit assignment we will not be forced to
    // call checkPutField, hence InitialTasks could be set more than once
    // for a thread.
    TaskLocal.TaskLocalMap taskLocals = null;
}
