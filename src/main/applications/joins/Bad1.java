package applications.joins;

import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;

// Not using Task.join but Thread.join -- bad!
public class Bad1 {

  {
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
  }

  // Result not final -- actions in thread happen-before thread.join()
  // Made private on purpose
  static class ResultHolder {
    int data;

    public ResultHolder(int data) {
      this.data = data;
    }
  }

  // Encapsulate thread and data
  static class TaskThread {
    Thread thread;
    ResultHolder result;
    final int initial;

    public TaskThread(int i) {
      this.initial = i;
      thread = new Thread(Task.ofThreadSafe(() -> {
        // Private to current task
        result = new ResultHolder(i);
      }));

      ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
    }

    void start() {
      thread.start();
    }

    void join() throws InterruptedException {
      thread.join();
    }

    ResultHolder getResult() {
      return result;
    }

    int initialCount() {
      return initial;
    }
  }

  void start(final int nbThreads) throws InterruptedException {
    TaskThread[] threads = new TaskThread[nbThreads];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new TaskThread(i);
      threads[i].start();
    }

    for (TaskThread thread : threads) {
      thread.join();

      // Task (thread) died, master *is* able to access data (JMM)
      // Will fail
      assert thread.initialCount() == thread.getResult().data;
    }
  }

  public static void main(String[] args) throws InterruptedException {
    new Bad1().start(10);
  }

}
