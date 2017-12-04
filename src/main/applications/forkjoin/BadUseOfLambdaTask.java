package applications.forkjoin;

import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permissions;
import icp.core.Task;

public class BadUseOfLambdaTask {
  // private to object
  private int attack;
  private Object objectAttack;
  private Object[] arrayAttack;

  public BadUseOfLambdaTask() throws InterruptedException {
    attack = 1;
    objectAttack = new Object();
    arrayAttack = new Object[1];

    Thread thread1 = new Thread(Task.ofThreadSafe(() -> {
      try {
        attack = 2;
        throw new AssertionError("Made it to here");
      } catch (IntentError good) {
      }

      //objectAttack.toString();
      //arrayAttack[0] = null;

    }));
    Thread thread2 = new Thread(Task.ofThreadSafe(() -> {
      try {
        attack = 3;
        throw new AssertionError("Made it to here");
      } catch (IntentError good) {
      }
    }));
    thread1.start();
    thread2.start();
    thread1.join();
    thread2.join();

    System.out.println(attack);
  }

  public static void main(String[] args) throws InterruptedException {
    BadUseOfLambdaTask app = new BadUseOfLambdaTask();
  }
}
