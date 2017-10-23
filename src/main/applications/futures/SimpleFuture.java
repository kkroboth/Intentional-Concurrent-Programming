package applications.futures;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class SimpleFuture {

  void start() throws ExecutionException, InterruptedException {
    Future<String> future = ForkJoinPool.commonPool().submit(() -> {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      // TODO: Strings work because tehy are not checked
      return "Hello World";
    });

    assert future.get().equals("Hello World");
  }

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    new SimpleFuture().start();
  }
}
