package applications.futures;

import icp.lib.OneTimeLatchRegistration;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

/**
 * Does not use a Task when submitting to pool.
 */
public class Bad1 {
  private OneTimeLatchRegistration latch;
  private int data;

  {
    latch = new OneTimeLatchRegistration();
  }

  void start() throws ExecutionException, InterruptedException {
    latch.registerWaiter();

    Future f = ForkJoinPool.commonPool().submit(() -> {
      try {
        latch.registerOpener(); // Fails here as Task.currentTask is used
        data = 42;
        latch.open();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });


    latch.await();
    assert data == 42;
  }

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    new Bad1().start();
  }

}
