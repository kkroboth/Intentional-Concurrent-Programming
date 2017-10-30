package applications.futures;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

/**
 * Does not use Callable task
 */
public class Bad2 {

  static class Data {
    int num;

    public Data(int data) {
      this.num = num;
    }
  }


  void start() throws ExecutionException, InterruptedException {
    Future<Data> f = ForkJoinPool.commonPool().submit(() -> {
      try {
        return new Data(42);
      } catch (Exception e) {
        e.printStackTrace();
      }

      throw new AssertionError("Should not reach");
    });

    assert f.get().num == 42;
  }

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    new Bad2().start();
  }

}
