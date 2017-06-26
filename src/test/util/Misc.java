// $Id$
package util;

import icp.core.IntentError;
import icp.core.Task;
import icp.core.Thread;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Misc {

  private Misc() {
    throw new AssertionError("cannot be instantiated");
  }

  public static Exception executeInNewICPThread(Runnable r) throws Exception {
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

  public static Exception executeInNewJavaThread(Runnable r) throws Exception {
    AtomicReference<Exception> ex = new AtomicReference<>();
    java.lang.Thread runner = new java.lang.Thread(() -> {
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

  public static int executeInNewICPThreads(Runnable[] tasks) throws Exception {
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
}
