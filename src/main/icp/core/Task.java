// $Id$

package icp.core;

import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Top level abstract Task class.
 * Provide public access to Task.currentTask, Task.holdsLock and
 * Task.getFirstInitialTask.
 * Default access for inheritance and lower level static methods.
 */
public class Task implements Runnable {
  private final Runnable theTask;
  private final AtomicBoolean running;
  private volatile CountDownLatch joinLatch;

  // Join permission
  private final Permission joinPermission;
  private final TaskLocal<Boolean> joiners;

  /*
   * Overview:
   *
   * Permissions are asserted by Tasks and not Threads. This allows a single thread
   * which may inaccessible to the user in thread pools, to run independent runnables
   * wrapped inside Tasks.
   *
   * A such scenario that can be caught by Tasks and not Threads is forgetting to unlock
   * a j.u.c.locks.Lock between multiple submitted jobs in an executor. If a single worker
   * executor thread has two jobs and job1 acquires the lock, without releasing it, job2
   * will be thrown an IntentError when trying to acquire. Depending on what type of executor,
   * an error may not be caught without ICP.
   *
   * Although Task is-a runnable, it does have thread-like methods including join. A better
   * name would be await() to not confuse with Thread.join(). Ideally it would be nice to
   * set a join permission on java.lang.Thread. Instead the join permission is here in Task.
   *
   * When extending this class, the default constructor is mainly for InitTask, and in most
   * cases the Task(Runnable) should be used. A complication implementing FutureTask happened
   * that required passing in a RunnableFuture to super(), but also storing the field. super()
   * must be the first statement. For now, a dummy static runnable is passed in and never used.
   * The package-private method doRun() can be overridden for custom execution. By default it
   * executes the task passed in constructor.
   */


  {
    // Can't use Utils.newBooleanTaskLocal as it will
    // set instance to frozen permission before an Init Task exists.
    joiners = new TaskLocal<Boolean>() {
      @Override
      protected Boolean initialValue() {
        return false;
      }
    };

    joinLatch = new CountDownLatch(1);

    joinPermission = new SingleCheckPermission("task not joined") {
      @Override
      protected boolean singleCheck() {
        Task curTask = Task.currentTask();
        // Either the task is still running (not joined) and
        // current task is *this* task.
        // Or, *this* task registered to join and *this*
        // task has joined.

        // TODO: Remove checking the count
        return (curTask.equals(Task.this) && (joinLatch.getCount() == 1))
          || (joiners.get() && joinLatch.getCount() == 0);
      }
    };
  }

  /**
   * Constructor should only be used in InitTask as it is bootstrapped.
   * Never will call run() method.
   */
  Task() {
    theTask = null;
    running = null;
  }

  /**
   * Create new ICP Task with given Runnable.
   *
   * @param task Runnable which will be run by {@link #run()}
   */
  Task(Runnable task) {
    if (task == null)
      throw new NullPointerException();
    theTask = task;
    running = new AtomicBoolean();
  }

  /**
   * Create new task from Thread-Safe Runnable.
   * A task is considered thread-safe iff that task's permission is of
   * {@link Permissions#getPermanentlyThreadSafePermission()}.
   * <p>
   * Lambdas are thread-safe and you should use this factory method when
   * constructing Tasks out of lambda runnables.
   *
   * @param task Thread safe runnable
   * @return New created task
   */
  public static Task ofThreadSafe(Runnable task) {
    return new Task(task);
  }


  /**
   * Passed in Runnable task is private and will be changed to
   * PermanentlyThreadSafe permission.
   *
   * @param task Private task runnable
   * @return New created task
   */
  public static Task ofPrivate(Runnable task) {
    // TODO: Transfer or thread-safe? Are we assuming any Runnable passed in
    // must be safe.
    ICP.setPermission(task, Permissions.getPermanentlyThreadSafePermission());
    return new Task(task);
  }

  /**
   * Runs the task.  Memory barriers ensure that a task that is re-run will see the effects
   * of its previous run.  There is no such guarantee across tasks.  It is invalid for multiple
   * threads to run a task concurrently.  Note that this enable tasks to have the same property
   * that threads have (i.e., they can see their own writes), even across multiple (but not
   * concurrent) executions by different threads.
   * <p>
   * {@link #doRun()} should be overridden if custom functionality is required. This method delegates
   * running <em>theTask</em> passed in constructor to <em>doRun</em>.
   */
  public void run() {
    assert running != null; // only null in init task, which is not run
    if (running.getAndSet(true))
      throw new IntentError("task already running");
    Task current = CURRENT_TASK.get();
    CURRENT_TASK.set(this);
    try {
      // optional overridden method
      doRun();
    } finally {
      CURRENT_TASK.set(current);
      running.set(false);
      joinLatch.countDown();
    }
  }

  /**
   * Override for custom task run behavior.
   * Default implementation runs the passed in Runnable from constructor.
   * <p>
   * One example is FutureTask does not pass in a Runnable, but calls its
   * underlying future run method.
   */
  void doRun() {
    assert theTask != null;
    theTask.run();
  }

  /**
   * Same semantics of a Thread.join() but for tasks.
   * A call to run() happens-before a thread returning from join().
   */
  // TODO: Consider calling this method await() to not confuse with Thread.join()
  public void join() throws InterruptedException {
    joiners.set(true);
    joinLatch.await();
  }

  /**
   * Get the join permission associated with task.
   * <p>
   * <em>Violations:</em>
   * <ul>
   * <li>Task did not call <em>join</em></li>
   * <li>Joining task has not joined</li>
   * </ul>
   *
   * @return the join permission
   */
  public Permission getJoinPermission() {
    return joinPermission;
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
   * @return true if the current task holds the lock.
   */
  static boolean holdsLock(Object object) {
    if (Thread.holdsLock(object)) {
      MonitorInfo[] info = MONITOR_INFO.get();
      if (info != null) {
        MonitorInfo[] currentLockedMonitors = getLockedMonitors();
        if (currentLockedMonitors.length > info.length) {

          for (MonitorInfo m : info) {
            for (int i = 0; i < currentLockedMonitors.length; i++) {
              if (m == currentLockedMonitors[i]) {
                currentLockedMonitors[i] = null;
                break;
              }
            }
          }

          MonitorInfo target = null;
          for (MonitorInfo m : currentLockedMonitors) {
            if (m != null) {
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
  protected static MonitorInfo[] getLockedMonitors() {
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
