// $Id: TestApp2.java 9 2017-06-05 20:23:55Z pjh $
//
// test the ICP core
// 

import icp.core.*;

import icp.lib.Thread;

// this class will be loaded by the normal class loader
public class TestCore
{
  public static void main(String[] args) throws Throwable
  {
    // initialize ICP and invoke the "real" user main method
    ICP.initialize("TestCore$Inner", args);
  }

  // this inner class will not be loaded until its main method is
  // invoked by the Javassist "run" method and so it will be loaded
  // by the Javassist class loader with our class editing
  public final static class Inner
  {
    public static void main(String[] args)
    {
      testPrivate();
      testSetPermission();
      testSamePermissionAs();
      testChainPermission();
    }

    private static void testPrivate()
    {
      // create an instance of an object that will be private to the
      // current thread
      TestClass t = new TestClass(42);

      // create lambda to access the object
      Runnable r = () -> {
        assert(t.getX() == 42);
        t.y = 1999; 
        assert(t.y == 1999);
      };

      // task that created the object should be able to access it
      r.run();

      // but access by a different task should generate an intent error
      // create lambda to be run by a different task
      r = () -> {
        expectFailureForAllAccesses(t);
      };
      new Thread(r).start();
    }

    private static void expectFailureForAllAccesses(TestClass t)
    {
      boolean caughtIntentError = false;
      try {
        assert(t.getX() == 42);
      }
      catch (IntentError ie)
      {
        caughtIntentError = true;
      }
      assert(caughtIntentError);
      caughtIntentError = false;
      try {
        t.y = 1914;
      }
      catch (IntentError ie)
      {
        caughtIntentError = true;
      }
      assert(caughtIntentError);
      caughtIntentError = false;
      try {
        assert(t.y == 1914);
      }
      catch (IntentError ie)
      {
        caughtIntentError = true;
      }
      assert(caughtIntentError);
    }

    private static void testSetPermission()
    {
      // create a permission where all accesses will fail
      Permission permission = AlwaysFailsPermission.newInstance();

      // create an object and replace its private permission with an
      // always-fail permission
      TestClass obj = new TestClass(42);
      ICP.setPermission(obj, permission);

      // all accesses should now fail
      expectFailureForAllAccesses(obj);

      // also should fail when setPermission is tried
      boolean caught = false;
      try {
        ICP.setPermission(obj, permission);
      }
      catch (IntentError ie)
      {
        caught = true;
      }
      assert(caught);
    }

    private static void testSamePermissionAs()
    {
      // create a frozen permission
      Permission permission = FrozenPermission.newInstance();

      // create an object and replace its private permission with a
      // frozen permission
      TestClass obj = new TestClass(42);
      ICP.setPermission(obj, permission);

      // create a second object and set its permission from the first object
      TestClass obj2 = new TestClass(42);
      ICP.samePermissionAs(obj2, obj);

      // put access should now fail for the second obj
      boolean caught = false;
      try {
        obj2.y = 1999;
      }
      catch (IntentError ie)
      {
        caught = true;
      }
      assert(caught);

      // also should fail when setPermission is tried on second object
      caught = false;
      try {
        ICP.setPermission(obj2, permission);
      }
      catch (IntentError ie)
      {
        caught = true;
      }
      assert(caught);
    }

    private static void testChainPermission()
    {
      // create a frozen permission
      Permission frozen = FrozenPermission.newInstance();

      // create an always fails permission
      Permission always = AlwaysFailsPermission.newInstance();

      // create an object, which will have a private permission
      // and chain on the frozen permission
      TestClass obj = new TestClass(33);
      ICP.chainPermission(obj, frozen);

      // call and get should work
      assert(obj.getX() == 33);
      assert(obj.y == 0);

      // but put should fail
      boolean caught = false;
      try {
        obj.y = 1999;
      }
      catch (IntentError ie)
      {
        caught = true;
      }
      assert(caught);
      

      // create an object, which will have a private permission
      // and chain on the always fails permission
      obj = new TestClass(43);
      ICP.chainPermission(obj, always);

      // and now nothing should work
      expectFailureForAllAccesses(obj);

      // including setPermission
      caught = false;
      try {
        ICP.setPermission(obj, always);
      }
      catch (IntentError ie)
      {
        caught = true;
      }
      assert(caught);

      // again create an object, which will have a private permission,
      // and then chain on a private permission.
      obj = new TestClass(43);
      ICP.chainPermission(obj, PrivatePermission.newInstance());

      // call and get should work
      assert(obj.getX() == 43);
      assert(obj.y == 0);

      // and I should be able to reset the chain permission
      ICP.setPermission(obj, AlwaysFailsPermission.newInstance());

      // and now nothing should work
      expectFailureForAllAccesses(obj);
    }
  }
}

