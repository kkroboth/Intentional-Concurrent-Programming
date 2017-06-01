// $Id$
//
// simple test to generate an intent error
// 

import icp.core.ICP;

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
      Runnable r = () -> System.out.println(t.getX());

      // thread that created the object should be able to access it
      r.run();

      // but access by a new thread should generate an intent error
      (new Thread(r)).start();
    }
  }
}

