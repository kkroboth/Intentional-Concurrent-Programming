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
    testPrivate();
    testSetPermission();
    testSamePermissionAs();
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
    assert(obj.getX() == 42);
    boolean caught = false;
    try {
      obj.y = 1999;
    }
    catch (IntentError ie)
    {
      caught = true;
    }
    assert(caught);

    // create a second object and set its permission to point to the
    // first object
    TestClass obj2 = new TestClass(43);
    ICP.samePermissionAs(obj2, obj);
    assert(obj.getX() == 42);
    assert(obj2.getX() == 43);

    // put access should now fail for the second obj
    caught = false;
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

    // create two objects and make the second have a same-as permission
    // to the first
    obj = new TestClass(44);
    obj2 = new TestClass(45);
    ICP.samePermissionAs(obj2, obj);

    // now change the permission of the first object to be always-fails
    ICP.setPermission(obj, AlwaysFailsPermission.newInstance());

    // calls to the second object should now fail
    caught = false;
    try {
      assert(obj2.getX() == 43);
    }
    catch (IntentError ie)
    {
      caught = true;
    }
    assert(caught);
  }
}

