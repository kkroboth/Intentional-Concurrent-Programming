package applications;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class Utils {
  private Utils() {

  }

  public static ThreadFactory logExceptionThreadFactory() {
    return new ThreadFactory() {
      AtomicInteger count = new AtomicInteger();

      @Override
      public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, "worker-" + count.getAndIncrement());
        thread.setUncaughtExceptionHandler((t, e) -> {
          e.printStackTrace();
        });
        return thread;
      }

    };
  }
}
