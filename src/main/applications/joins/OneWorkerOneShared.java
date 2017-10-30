package applications.joins;

import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Task;

public class OneWorkerOneShared {
  // Shared operation (thread safe)
  static class Data {
    int counter = 0;
  }

  final Data data;

  OneWorkerOneShared() {
    data = new Data();
  }


  void start() throws InterruptedException {
    Task task = Task.fromThreadSafeRunnable(() -> {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      data.counter = 1;
    });
    // set join permission on data
    ICP.setPermission(data, task.getJoinPermission());

    // Test join permission (bad)
    try {
      System.out.println(data.counter);
      throw new AssertionError("Main task accessed data before join");
    } catch (IntentError good) {
    }

    // throw away access to thread -- use task.join()
    new Thread(task).start();

    // join and get access to data
    task.join();
    assert data.counter == 1;
  }

  public static void main(String[] args) throws InterruptedException {
    OneWorkerOneShared app = new OneWorkerOneShared();
    app.start();
  }

}
