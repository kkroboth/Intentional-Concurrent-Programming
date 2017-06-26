// $Id$
package applications.pi1;

//
// compute Pi using only SimpleReentrantLock
//

import icp.core.ICP;
import icp.core.Task;
import icp.core.Thread;
import icp.lib.SimpleReentrantLock;

// this class will be loaded by the normal class loader
public class Pi1
{

  private static class Pi1SharedValue {
    public int i;
    public double d;
  }

  public static void main(String[] args) throws Throwable
  {
    if (args.length == 0)
    {
      throw new RuntimeException("usage: Pi1 numberOfTasks");
    }

    // get the number of tasks to use from the command line
    final int n;
    try {
      n = Integer.parseInt(args[0]);
    }
    catch (NumberFormatException nfe)
    {
      throw new RuntimeException("usage: Pi numberOfTasks");
    }

    // control for distributing the intervals
    final int INTERVALS = 50000000;
    final double width = 1.0 / INTERVALS;
    int ch = INTERVALS / n;
    int sp = INTERVALS % n;
    if (sp == 0)
    {
      sp = n;
      ch -= 1;
    }
    final int chunk = ch;
    final int split = sp;

    // create a lock to protect the shared global sum
    final SimpleReentrantLock lock = new SimpleReentrantLock();

    // create a object to contain the shared global sum
    final Pi1SharedValue sum = new Pi1SharedValue();

    // make the global sum shareable by attaching the lock's
    // permission to it
    ICP.setPermission(sum, lock.getLockedPermission());

     // create and start a set of tasks to do the pi computation
    for (int i = 0; i < n; i++)
    {
      final int id = i;

      // create the task
      Task task = Task.fromThreadSafeRunnable(() -> {
        int low;
        int high;
        if (id < split) {
          low = (id * (chunk + 1));
          high = low + (chunk + 1);
        } else {
          low = (split * (chunk + 1)) + ((id - split) * chunk);
          high = low + chunk;
        }

        double localSum = 0.0;
        double x = (low + 0.5) * width;
        for (int j = low; j < high; j++) {
          localSum += (4.0 / (1.0 + x * x));
          x += width;
        }

        // critical section to update the shared global sum
        lock.lock();
        sum.i += 1;
        sum.d += localSum;
        if (sum.i == n) // last task will check answer
        {
          assert(String.format("%14.12f",
            sum.d * width).startsWith("3.141592653"));
        }
        lock.unlock();
      });

      // start the task on a new Thread
      new Thread(task).start();
    }
  }
}

