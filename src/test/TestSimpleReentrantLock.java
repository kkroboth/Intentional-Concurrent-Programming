// $Id$

import core.TestClass;
import icp.core.*;
import icp.lib.Thread;
import icp.lib.SimpleReentrantLock;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

//@RunWith(JUnitParamsRunner.class)
public class TestSimpleReentrantLock
{
  public static void main(String[] args) throws Throwable
  {
    // create an object and protect it with a lock
    final int VALUE = 42;
    final TestClass obj = new TestClass(VALUE);
    final SimpleReentrantLock lock = SimpleReentrantLock.newInstance();
    ICP.setPermission(obj, lock.lockedPermission());

    // create a task to try to access the object without the lock
    Runnable task = () -> {
      boolean caught = false;
      try {
        int x = obj.getX();
      }
      catch (IntentError ie)
      {
        caught = true;
      }
      assertTrue(caught);
      caught = false;
      try {
        int x = obj.y;
      }
      catch (IntentError ie)
      {
        caught = true;
      }
      assertTrue(caught);
      caught = false;
      try {
        obj.y = 19;
      }
      catch (IntentError ie)
      {
        caught = true;
      }
      assertTrue(caught);
      caught = false;
      try {
        ICP.setPermission(obj, lock.lockedPermission());
      }
      catch (IntentError ie)
      {
        caught = true;
      }
      assertTrue(caught);
    };
    new Thread(task).start();

    // create a task to update the object and execute it with
    // a set of N threads
    final int N = 4;
    task = () -> {
      lock.lock();
      obj.y = (obj.y + obj.getX());
      lock.unlock();
    };
    Thread t[] = new Thread[N];
    for (int i = 0; i < N; i++)
    {
      t[i] = new Thread(task);
      t[i].start();
    }
    for (int i = 0; i < N; i++)
    {
      try {
        t[i].join();
      }
      catch (InterruptedException ie)
      {
        assertTrue(false, "caught InterruptedException with join");
      }
    }
    lock.lock();
    assertEquals(obj.y, VALUE * N);

    // even holding the lock, I should not be able to reset the permission
    TestClass obj2 = new TestClass(37);
    boolean caught = false;
    try {
      ICP.samePermissionAs(obj, obj2);
    }
    catch (IntentError ie)
    {
      caught = true;
    }
    assertTrue(caught);
    lock.unlock();
  }

}

