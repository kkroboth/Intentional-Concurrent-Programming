// $Id: DemoSuite.java 17 2017-06-19 16:59:12Z charpov $
package core;

import icp.core.DoNotEdit;
import icp.core.IntentError;
import icp.core.ICP;
import icp.core.Permissions;
import org.testng.annotations.Test;
import util.ICPTest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static util.Misc.executeInNewICPThread;

@DoNotEdit
public class TestLanguage extends ICPTest {

  @Test(description = "inheritance from abstract class, abstract method")
  public void testInheritAbstractAbstract() throws Exception {

    abstract class A {
      abstract public void call();
    }

    class C extends A {
      public void call() {
      }
    }

    Runnable r = () -> new C().call();

    assertNull(executeInNewICPThread(r));
  }

  @Test(description = "inheritance from abstract class, concrete method")
  public void testInheritAbstractConcrete() throws Exception {

    abstract class A {
      public void call() {
      }
    }

    class C extends A {
    }

    Runnable r = () -> new C().call();

    assertNull(executeInNewICPThread(r));
  }

  @Test(description = "inheritance from abstract class, concrete method, overridden")
  public void testInheritAbstractOverride() throws Exception {

    abstract class A {
      public void call() {
      }
    }

    class C extends A {
      @Override
      public void call() {
        super.call();
      }
    }

    Runnable r = () -> new C().call();

    assertNull(executeInNewICPThread(r));
  }

  @Test(description = "inheritance from concrete class, no override")
  public void testInheritConcrete() throws Exception {

    class A {
      public void call() {
      }
    }

    class C extends A {
    }

    Runnable r = () -> new C().call();

    assertNull(executeInNewICPThread(r));
  }

  @Test(description = "inheritance from concrete class, overridden")
  public void testInheritConcreteOverride() throws Exception {

    class A {
      public void call() {
      }
    }

    class C extends A {
      @Override
      public void call() {
        super.call();
      }
    }

    Runnable r = () -> new C().call();

    assertNull(executeInNewICPThread(r));
  }

  @Test(description = "inherited class loaded first")
  public void testInheritFirst() throws Exception {
    class A {
    }
    class B extends A {
    }
    ICP.setPermission(new B(), Permissions.getFrozenPermission());
  }

  @Test(description = "inherited class loaded second")
  public void testInheritSecond() throws Exception {
    class A {
    }
    class B extends A {
    }
    new A();
    ICP.setPermission(new B(), Permissions.getFrozenPermission());
  }

  @Test(description = "final field is always accessible")
  public void testFinal() throws Exception {
    class A {
      int i = 13;
    }
    class B {
      public A nf = new A();
      public final A f = new A();
    }
    // create object and make it inaccessible
    B obj = new B();
    ICP.setPermission(obj, Permissions.getNoAccessPermission());
    // read of non-final field should fail
    Runnable read = new Runnable() { // cannot use lambdas
      public void run() {
        System.out.println(obj.nf.i);
      }
    };
    assertThrows(IntentError.class, read::run);
    // read of final field should execute
    read = new Runnable() { // cannot use lambdas
      public void run() {
        assertEquals(obj.f.i, 13);
      }
    };
    read.run();
  }

  @Test(description = "concrete methods and constructors in abstract classes")
  public void testAbstract() throws Exception {
    abstract class A {
      A() {
        TestClass t = new TestClass();
        ICP.setPermission(t, Permissions.getNoAccessPermission());
        // getfield should be checked
        System.out.println(t.x);
      }
      A(int i) {
        TestClass t = new TestClass();
        ICP.setPermission(t, Permissions.getNoAccessPermission());
        // putfield should be checked
        t.x = 42;
      }
      A(int i, int j) {
        // this constructor will not fail
      }
      void meth1() {
        TestClass t = new TestClass();
        ICP.setPermission(t, Permissions.getNoAccessPermission());
        // field references should be checked in this method
        System.out.println(t.x);
      }
      void meth2() {
        TestClass t = new TestClass();
        ICP.setPermission(t, Permissions.getNoAccessPermission());
        // field references should be checked in this method
        t.x = 42;
      }
      void meth3() {
      }
    }
    class B extends A {
      B() {
      }
      B(int i) {
       super(i);
      }
      B(int i, int j) {
       super(i, j);
      }
    }
    assertThrows(IntentError.class, () -> new B());
    assertThrows(IntentError.class, () -> new B(1));
    B b = new B(1,2);
    assertThrows(IntentError.class, b::meth1);
    assertThrows(IntentError.class, b::meth2);
    ICP.setPermission(b, Permissions.getNoAccessPermission());
    // should be check at the beginning of the method
    assertThrows(IntentError.class, b::meth3);
  }

  @Test(description = "default methods in interfaces")
  public void testInterface() throws Exception {
    class A implements TestInterface {
    }
    A a = new A();
    assertThrows(IntentError.class, a::meth1);
    assertThrows(IntentError.class, a::meth2);
    ICP.setPermission(a, Permissions.getNoAccessPermission());
    // should be check at the beginning of the method
    assertThrows(IntentError.class, a::meth3);
  }
  
}
