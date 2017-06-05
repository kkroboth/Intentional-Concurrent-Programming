// $Id$
//
// simple test to generate an intent error
// 

import icp.core.ICP;
import icp.core.IntentError;

import icp.lib.Thread;

// this class will be loaded by the normal class loader
public class TestApp2
{
  public static void main(String[] args) throws Throwable
  {
    // initialize ICP and invoke the "real" user main method
    ICP.initialize("TestApp2$Inner", args);
  }

  // this inner class will not be loaded until its main method is
  // invoked by the Javassist "run" method and so it will be loaded
  // by the Javassist class loader with our class editing
  public final static class Inner
  {
    public static void main(String[] args)
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
      };
      new Thread(r).start();
    }
  }
}

