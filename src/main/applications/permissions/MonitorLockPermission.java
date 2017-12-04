package applications.permissions;

import icp.core.ICP;
import icp.core.Permissions;

public class MonitorLockPermission {
  public static class Data {
    private int value;
  }

  private Data data;

  public MonitorLockPermission() {
    data = new Data();
    ICP.setPermission(data, Permissions.getHoldsLockPermission(this));
  }

  void start() throws InterruptedException {
    Thread thread = new Thread(() -> {
      synchronized (MonitorLockPermission.this) {
        data.value++;
      }
    });
    thread.join();
  }

  public static void main(String[] args) throws InterruptedException {
    MonitorLockPermission app = new MonitorLockPermission();

    app.start();
    synchronized (app) {
      System.out.println(app.data.value);
    }
  }
}
