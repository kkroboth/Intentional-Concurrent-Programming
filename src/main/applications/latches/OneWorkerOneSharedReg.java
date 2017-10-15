package applications.latches;

import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.OneTimeLatchRegistration;

/**
 * One master and One working. Master waits on a 1-time
 * latch with registration.
 * <p>
 * This scenario is valid because there is only one worker who
 * is a registered opener. Once latch is open, the provider can't
 * be touched by the worker.
 */
public class OneWorkerOneSharedReg {

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

  OneTimeLatchRegistration latch;
  final Data data;
  final Provider provider;
  final Accessor accessor;

  OneWorkerOneSharedReg() {
    latch = new OneTimeLatchRegistration();
    data = new Data();
    provider = new Provider(data);
    accessor = new Accessor(data);

    ICP.setPermission(accessor, latch.getPermission());
    ICP.setPermission(provider, latch.getPermission());
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
  }

  void start() throws InterruptedException {
    latch.registerWaiter();

    new Thread(Task.fromThreadSafeRunnable(() -> {
      latch.registerOpener();

      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      provider.setData(1);
      latch.open();
      try {
        provider.setData(500);
        throw new AssertionError("Worker was able to touch data after latch opened.");
      } catch (Exception ignore) {
      }
    })).start();

    latch.await();
    assert accessor.getCounter() == 1;
  }


  public static void main(String[] args) throws InterruptedException {
    OneWorkerOneSharedReg app = new OneWorkerOneSharedReg();
    app.start();
  }

}
