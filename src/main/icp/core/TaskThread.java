package icp.core;

/**
 * One to one relation between thread running the current task.
 *
 * <h1>Using this class and underlying java.lang.Thread</h2>
 * <p>
 * Using the underlying passed in thread is acceptable as long as you know
 * what you are doing. Calling Thread.join() and TaskThread.join()
 * </p>
 *
 * Currently, this implementation is only required in special cases:
 * * Joining tasks
 *
 * <h1>Joining Tasks</h1>
 * <p>
 * Using just a runnable (task) and normal Java Thread, it is impossible to
 * access a finished task's target object without explicit and correct use
 * of Transfer permission. It gets more complicated on multiple tasks joining.
 * </p>
 * <p>
 * Consider the following scenario:
 * <pre>
 * <code>
 * // Fork-Join example:
 * Target target = new Target();
 *
 * ICP.setPermission(target, &lt;permission associated with synchronize&gt;)
 * // Set up multiple tasks and threads that allow use of target
 *
 * // wait for tasks to finish
 * for(Thread t : threads) t.join();
 *
 * // Access target data
 * target.getData();
 * </code>
 * </pre>
 * This will fail at <code>target.getData();</code> because the permission is lost after
 * all tasks have finished. Through happens-before relations, after joining all threads,
 * you will be able to see the data modified by all tasks regardless. But our ICP system
 * will through a violation error.
 * </p>
 *
 * <em>Synchronization:</em> Methods start(), join() are synchronized with underlying thread
 * object and NOT the TaskThread instance.
 */
public final class TaskThread {
  final Thread thread;
  private final Task task; // Currently not used, but good to show tight coupling?
  private Object[] targets;

  /**
   * Creates TaskThread and underlying thread out of
   * task. Same as doing:
   * <pre>
   * <code>
   * Task task = mytask;
   * ThreadTask threadTask = new ThreadTask(new Thread(task), task);
   * </code>
   * </pre>
   *
   * @param task Task to create TaskThread out of
   * @return created TaskThread
   */
  public static TaskThread of(Task task) {
    return new TaskThread(new Thread(task), task);
  }

  // Register join() calls
  private final TaskLocal<Boolean> joinRegistered = new TaskLocal<Boolean>() {
    @Override
    protected Boolean initialValue() {
      return Boolean.FALSE;
    }
  };


  /**
   * Create TaskThread pair of task and its thread.
   *
   * @param thread thread which will be running the task
   * @param task   Task run by the thread
   * @throws IllegalStateException If passed in thread is not in NEW state.
   */
  public TaskThread(Thread thread, Task task) {
    this.thread = thread;
    this.task = task;
    if (thread.getState() != Thread.State.NEW)
      throw new IllegalStateException("Thread should not be started");
  }

  /**
   * Start underlying thread and keep track of targets
   * for which can be accessed after join() returns.
   *
   * @param targets Array of objects which can be accessed
   *                after join() returns
   */
  public void start(Object... targets) {
    synchronized (thread) {
      this.targets = targets;
      thread.start();
    }
  }

  void setTargets(Object... targets) {
    synchronized (thread) {
      this.targets = targets;
    }
  }

  Object[] getTargets() {
    synchronized (thread) {
      return targets;
    }
  }

  public void join() throws InterruptedException {
    synchronized (thread) {
      if (targets == null) {
        throw new IntentError("Called join() before start()");
      }

      thread.join();

      // Reset permission to private (master thread)
      // TODO: Potential problem -- What happens if a target is still in use with
      // another Task? We will need a TaskThreadGroup for those cases.
      for (Object target : targets) {
        PermissionSupport.forceSetPermission(target, Permissions.getPrivatePermission());
      }
    }
  }

}
