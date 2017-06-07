// $Id$

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import icp.core.*;
import icp.lib.Thread;
import icp.lib.SimpleReentrantLock;

//@RunWith(JUnitParamsRunner.class)
public class TestSimpleReentrantLock
{
  //@Test
  public void test1() throws Throwable
  {
    ICP.initialize("TestSimpleReentrantLock$Test1", new String[0]);
  }

  public static void main(String[] args) throws Throwable
  {
    ICP.initialize("TestSimpleReentrantLock$Test1", new String[0]);
  }

  public final static class Test1
  {
    public static void main(String[] args)
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
          assertTrue("caught InterruptedException with join", false);
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

}

