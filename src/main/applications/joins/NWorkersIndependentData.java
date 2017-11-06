package applications.joins;

import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;

import java.util.concurrent.ThreadLocalRandom;

/**
 * There are "n" workers calculating data (word counts in files)
 * independently from all other tasks. The master waits (joins)
 * on all workers and iterates over to retrieve each data.
 */
public class NWorkersIndependentData {

  static class Data {
    int result;

    Data() {
      ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
    }
  }

  static class Accessor {
    final Data data;


    Accessor(Data data) {
      this.data = data;
    }

    int getResult() {
      return data.result;
    }
  }

  // Encapsulates task and independent data
  static class Worker {
    final Task task;
    final Data result;
    final Accessor accessor;

    Worker() {
      result = new Data();
      accessor = new Accessor(result);

      task = Task.ofThreadSafe(() -> {
        try {
          Thread.sleep(ThreadLocalRandom.current().nextInt(1, 50));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        result.result = 1;
      });

      ICP.setPermission(accessor, task.getJoinPermission());
      ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
    }

    void start() {
      new Thread(task).start();
    }

    Task getTask() {
      return task;
    }

    Accessor getAccessor() {
      return accessor;
    }

  }


  final int nbWorkers;

  NWorkersIndependentData(int nbWorkers) {
    this.nbWorkers = nbWorkers;
  }

  void start() throws InterruptedException {
    Worker[] workers = new Worker[nbWorkers];
    for (int i = 0; i < nbWorkers; i++) {
      workers[i] = new Worker();
      workers[i].start();
    }

    int sum = 0;
    for (int i = 0; i < nbWorkers; i++) {
      workers[i].getTask().join();
      sum += workers[i].getAccessor().getResult();
    }

    assert sum == nbWorkers;
  }

  public static void main(String[] args) throws InterruptedException {
    NWorkersIndependentData app = new NWorkersIndependentData(25);
    app.start();
  }

}
