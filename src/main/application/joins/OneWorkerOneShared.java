package application.joins;

import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;

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

  final Data data;
  final Provider provider;
  final Accessor accessor;

  OneWorkerOneShared() {
    data = new Data();
    provider = new Provider(data);
    accessor = new Accessor(data);
    ICP.setPermission(provider, Permissions.getPermanentlyThreadSafePermission());
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
  }


  void start() throws InterruptedException {
    Task task = Task.fromThreadSafeRunnable(() -> {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      provider.setData(1);
    });

    // throw away access to thread -- use task.join()
    new Thread(task).start();

    // set join permission on data
    ICP.setPermission(accessor, task.getJoinPermission());

    // join and get access to data
    task.join();
    assert accessor.getCounter() == 1;
  }

  public static void main(String[] args) throws InterruptedException {
    OneWorkerOneShared app = new OneWorkerOneShared();
    app.start();
  }

}
