package icp.core;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Encapsulates a Task which runs and handles Callable.
 * <p>
 * CallableTask <em>has-a</em> Task and not <em>is-a</em> relationship to disallow
 * methods <em>join</em> and others because j.u.c Callable is not directly related
 * to a Thread.
 * // TODO: (Kyle) Explain more why
 *
 * @param <V> Callable return type
 */
public class CallableTask<V> implements Callable<V> {

  /*
   * Overview:
   *
   * CallableTask is one solution for implementing Futures.
   * The user's callable must be executed in a Task, but Task
   * only supports runnables.
   *
   * This class creates a task and runnable and it is the runnable's
   * job to execute user's callable, and store result in a "box".
   * Now the call method may access the contents in box and return
   * result.
   */

  private final Task task; // underlying task who runs callable

  // Holds result computed by Callable.
  // Can be either a Exception or Object type of V.
  // Note: Not a normal volatile field as the result is set inside a runnable not created
  // in the constructor.
  private final AtomicReference<Object> box;

  // Must use factory methods
  private CallableTask(Runnable runnable, AtomicReference<Object> box) {
    task = new Task(runnable);
    this.box = box;
  }

  /**
   * Create a Callable task from a given thread-safe callable.
   *
   * @param callable thread-safe callable (lambda)
   * @param <V>      Callable return type
   * @return Callable task ready
   * @see Task#ofThreadSafe(Runnable)
   */
  public static <V> Callable<V> ofThreadSafe(Callable<V> callable) {
    AtomicReference<Object> box = new AtomicReference<>();
    Runnable r = () -> {
      try {
        box.set(callable.call());
      } catch (Exception e) {
        box.set(e);
      }
    };

    return new CallableTask<>(r, box);
  }

  /**
   * Create a Callable task from a given private callable.
   *
   * @param callable private callable
   * @param <V>      Callable return type
   * @return Callable task ready
   * @see Task#ofPrivate(Runnable)
   */
  public static <V> Callable<V> ofPrivate(Callable<V> callable) {
    ICP.setPermission(callable, Permissions.getPermanentlyThreadSafePermission());
    return ofThreadSafe(callable);
  }

  @Override
  public V call() throws Exception {
    // Note: Cannot use any Task related operations as the current
    // thread may not be a task
    task.run();
    Object result = box.get();
    if (result instanceof Exception) throw (Exception) result;

    //noinspection unchecked
    return (V) result;
  }
}
