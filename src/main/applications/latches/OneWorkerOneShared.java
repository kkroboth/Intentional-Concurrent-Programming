package applications.latches;

import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.OneTimeLatch;

/**
 * One master and One working. Master waits on a 1-time
 * latch which worker opens once it's done. Problem is once
 * the latch is opened, nothing stops the worker from
 * accessing the shared object again.
 */
public class OneWorkerOneShared {

  // Shared operation (thread safe)
  static class Data {
    int counter = 0;

    Data() {
      ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
    }
  }

  // Worker accesses (thread safe)
  static class Provider {
    final Data data;

    Provider(Data data) {
      this.data = data;
      ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
    }

    void setData(int counter) {
      this.data.counter = counter;
    }
  }

  // Accessor (is open permission)
  static class Accessor {
    final Data data;

    Accessor(Data data) {
      this.data = data;
    }

    int getCounter() {
      return data.counter;
    }
  }

  OneTimeLatch latch;
  final Data data;
  final Provider provider;
  final Accessor accessor;

  OneWorkerOneShared() {
    latch = new OneTimeLatch();
    data = new Data();
    provider = new Provider(data);
    accessor = new Accessor(data);

    ICP.setPermission(accessor, latch.getIsOpenPermission());
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
  }

  void start() throws InterruptedException {
    new Thread(Task.fromThreadSafeRunnable(() -> {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      provider.setData(1);
      latch.open();
      // Possible since provider has no permission (thread safe)
      // provider.setData(500);
    })).start();

    latch.await();
    assert accessor.getCounter() == 1;
  }


  public static void main(String[] args) throws InterruptedException {
    OneWorkerOneShared app = new OneWorkerOneShared();
    app.start();
  }

}
