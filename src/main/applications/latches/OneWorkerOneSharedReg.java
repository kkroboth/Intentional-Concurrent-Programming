package applications.latches;

import icp.core.ICP;
import icp.core.IntentError;
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
  }


  OneTimeLatchRegistration latch;
  final Data data;

  OneWorkerOneSharedReg() {
    latch = new OneTimeLatchRegistration();
    data = new Data();

    ICP.setPermission(data, latch.getPermission());
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
  }

  void start() throws InterruptedException {
    latch.registerWaiter();

    try {
      int i = data.counter;
      throw new AssertionError("Master was able to access before latch opened");
    } catch (IntentError good) {
    }

    new Thread(Task.fromThreadSafeRunnable(() -> {
      latch.registerOpener();

      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      data.counter += 1;
      latch.open();
      try {
        data.counter = 500;
        throw new AssertionError("Worker was able to touch data after latch opened.");
      } catch (Exception ignore) {
      }
    })).start();

    latch.await();
    assert data.counter == 1;
  }


  public static void main(String[] args) throws InterruptedException {
    OneWorkerOneSharedReg app = new OneWorkerOneSharedReg();
    app.start();
  }

}
