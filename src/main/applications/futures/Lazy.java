package applications.futures;

import icp.core.CallableTask;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * Test lazy evaluation using futures.
 * <p>
 * Scala's lazy val but using thread pool.
 */
public class Lazy {

  private static class Data {
    private final int number;

    public Data(int number) {
      this.number = number;
    }
  }

  Lazy() {
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
  }

  private LazyVal<Data> d1 = new LazyVal<>(() -> new Data(42));
  private LazyVal<Data> d2 = new LazyVal<>(() -> new Data(1));
  private LazyVal<Data> d3 = new LazyVal<>(() -> new Data(100));

  void start() {
    for (int i = 0; i < 10; i++) {
      new Thread(Task.ofThreadSafe(() -> {
        try {
          assert d1.get().number == 42;
          assert d2.get().number == 1;
          assert d3.get().number == 100;
        } catch (ExecutionException | InterruptedException e) {
          e.printStackTrace();
        }
      })).start();
    }
  }

  public static void main(String[] args) {
    new Lazy().start();
  }


  static final class LazyVal<T> {
    private Future<T> future;
    private final Supplier<T> callable;

    LazyVal(Supplier<T> callable) {
      this.callable = callable;
      future = null;
      ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
    }

    T get() throws ExecutionException, InterruptedException {
      Future<T> f;
      synchronized (this) {
        if (future == null) {
          future = ForkJoinPool.commonPool().submit(CallableTask.ofThreadSafe(this::calc));
        }
        f = future;
      }
      return f.get();
    }

    private T calc() {
      T result = callable.get();
      ICP.setPermission(result, Permissions.getFrozenPermission());
      return result;
    }
  }

}
