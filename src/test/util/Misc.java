// $Id$
package util;

import icp.core.IntentError;
import icp.core.Task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Helper static methods to wrap runnables inside Tasks or just normal java threads.
 */
public class Misc {

  private Misc() {
    throw new AssertionError("cannot be instantiated");
  }

  /**
   * Wraps given Runnable inside a Task using {@link Task#fromThreadSafeRunnable(Runnable)}
   * in a Java Thread. Will wait until thread joins before returning.
   *
   * @param r Runnable to wrap in Task
   * @return Thrown exception during execution of runnable or <code>null</code>
   * @throws InterruptedException joining thread
   */
  public static Exception executeInNewICPTaskThread(Runnable r) throws InterruptedException {
    AtomicReference<Exception> ex = new AtomicReference<>();
    Thread runner = new Thread(Task.fromThreadSafeRunnable(() -> {
      try {
        r.run();
      } catch (Exception e) {
        ex.set(e);
      }
    }));
    runner.start();
    runner.join();
    return ex.get();
  }

  /**
   * Runs given Runnable in a Java Thread, waits for it to join, and returns exception
   * thrown during execution or null.
   *
   * @param r Runnable to run in Java thread.
   * @return Exception thrown or null
   * @throws InterruptedException Joining thread
   */
  public static Exception executeInNewJavaThread(Runnable r) throws InterruptedException {
    AtomicReference<Exception> ex = new AtomicReference<>();
    java.lang.Thread runner = new Thread(() -> {
      try {
        r.run();
      } catch (Exception e) {
        ex.set(e);
      }
    });
    runner.start();
    runner.join();
    return ex.get();
  }

  /**
   * Similar to {@link Misc#executeInNewICPTaskThread(Runnable)} but with multiple Runnables. Will
   * wait for all Java threads to join before returning.
   *
   * @param tasks Array of Runnables to wrap in Tasks
   * @return Number of exceptions raised in any of the threads
   * @throws InterruptedException Joining all threads
   * @see #executeInNewICPTaskThread(Runnable)
   */
  public static int executeInNewICPTaskThreads(Runnable[] tasks) throws InterruptedException {
    int n = tasks.length;
    Thread[] runners = new Thread[n];
    AtomicInteger exceptions = new AtomicInteger();

    for (int i = 0; i < n; i++) {
      int id = i;
      Thread runner = new Thread(Task.fromThreadSafeRunnable(() -> {
        try {
          tasks[id].run();
        } catch (IntentError e) {
          exceptions.incrementAndGet();
        }
      }), "runner-" + i);
      runners[i] = runner;
      runner.start();
    }
    for (Thread t : runners)
      t.join();
    return exceptions.get();
  }

  public static Future<Integer> executeInNewICPTaskThreadsFuture(Runnable[] tasks) throws InterruptedException {
    int n = tasks.length;
    Thread[] runners = new Thread[n];
    AtomicInteger exceptions = new AtomicInteger();

    for (int i = 0; i < n; i++) {
      int id = i;
      Thread runner = new Thread(Task.fromThreadSafeRunnable(() -> {
        try {
          tasks[id].run();
        } catch (IntentError e) {
          e.printStackTrace();
          exceptions.incrementAndGet();
        }
      }), "runner-" + i);
      runners[i] = runner;
      runner.start();
    }

    // Wait for all runners in another thread to return future
    CompletableFuture<Integer> errorsFuture = new CompletableFuture<>();
    new Thread(() -> {
      for (Thread t : runners)
        try {
          t.join();
        } catch (InterruptedException e) {
          errorsFuture.completeExceptionally(e);
        }

      errorsFuture.complete(exceptions.get());
    }).start();

    return errorsFuture;
  }
}
