package core;// $Id$

import java.util.concurrent.atomic.AtomicReference;

public class Utils {

  private Utils() {
    throw new AssertionError("cannot be instantiated");
  }

  public static Exception executeInNewThread(Runnable r) throws Exception {
    AtomicReference<Exception> ex = new AtomicReference<>();
    Thread runner = new Thread(() -> {
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
}
