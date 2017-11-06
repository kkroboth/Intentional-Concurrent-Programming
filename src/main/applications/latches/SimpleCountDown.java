package applications.latches;

import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.CountDownLatch;

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleCountDown {

  static class Data {
    AtomicInteger counter = new AtomicInteger();
  }

  final CountDownLatch latch;
  final int count;
  final Data data;

  SimpleCountDown(int count) {
    this.data = new Data();
    this.count = count;
    latch = new CountDownLatch(count);

    ICP.setPermission(data, latch.getPermission());
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
  }

  void start() throws InterruptedException {
    latch.registerWaiter();

    // Worker
    for (int i = 0; i < count; i++) {
      new Thread(Task.ofThreadSafe(() -> {
        latch.registerCountDowner();
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        data.counter.incrementAndGet();
        latch.countDown();
        try {
          data.counter.incrementAndGet();
          throw new AssertionError("Worker worked after calling countdown");
        } catch (IntentError ignore) {
        }
      })).start();

    }

    latch.await();
    assert data.counter.get() == count;
  }


  public static void main(String[] args) throws InterruptedException {
    new SimpleCountDown(10).start();
  }
}
