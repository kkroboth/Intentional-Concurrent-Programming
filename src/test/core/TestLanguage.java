// $Id: DemoSuite.java 17 2017-06-19 16:59:12Z charpov $
package core;

import icp.core.External;
import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permissions;
import org.testng.annotations.Test;
import util.ICPTest;

import static org.testng.Assert.*;
import static util.Misc.executeInNewICPThread;

@External
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
    assertThrows(IntentError.class, () -> System.out.println(obj.nf.i));
    // read of final field should execute
    assertEquals(obj.f.i, 13);
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
    B b = new B(1, 2);
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

  @Test(description = "anonymous class captures a variable")
  public void testAnonymous() throws Exception {
    final TestClass a = new TestClass();
    a.callAndWrite(1066);
    ICP.setPermission(a, Permissions.getFrozenPermission());
    Runnable r = new Runnable() {
      public void run() {
        assertEquals(a.callAndRead(), 1066);
      }
    };
    ICP.setPermission(r, Permissions.getFrozenPermission());
    assertNull(executeInNewICPThread(r));
  }

  @Test(description = "nested classes")
  public void testNested() throws Exception {

    // create a thread-safe object with shared inner objects
    final NestedClasses a = new NestedClasses(1066);
    ICP.setPermission(a, Permissions.getPermanentlyThreadSafePermission());
    assertEquals(a.getX(), 1066);
    assertEquals(a.getY(), 2066);
    assertEquals(a.testLocal(), 1066 + 2066);

    // expose an instance to the inner class
    final Object b = a.exposeX();
    final Object c = a.exposeY();

    // expose instance of a local class
    final Object d = a.testLocal2(null);
    // should be able to use it again since this thread created it
    Object e = a.testLocal2(d);

    // now access the object via another thread
    Runnable r = new Runnable() {
      public void run() {
        // these will access both the outer and inner classes
        assertEquals(a.getX(), 1066);
        assertEquals(a.getY(), 2066);
        assertEquals(a.testLocal(), (1066 + 2066));
        a.incX();
        a.incY();
        assertEquals(a.getX(), 1067);
        assertEquals(a.getY(), 2067);
        assertEquals(a.grabX(), 1067);
        assertEquals(a.grabY(), 2067);

        // try to use local private object created by another thread
        boolean threwIt = false;
        try {
          a.testLocal2(d);
        } catch (IntentError ie) {
          threwIt = true;
        }
        assertTrue(threwIt);
      }
    };
    ICP.setPermission(r, Permissions.getFrozenPermission());
    assertNull(executeInNewICPThread(r));
  }

  // test that instance initializers are edited by Javassist
  @Test(description = "instance initializer")
  public void testInstanceInitializer() throws Exception {
    class A {
      public int i = 1066;
    }
    ;
    A x = new A();
    ICP.setPermission(x, Permissions.getNoAccessPermission());
    class B {
      B() {
        i = i + 13;
      }

      ;
      public int i = x.i + 37;
    }
    ;
    boolean threwIt = false;
    try {
      B b = new B();
    } catch (IntentError ie) {
      threwIt = true;
    }
    assertTrue(threwIt);
    class C {
      C() {
        i = i + 13;
      }

      ;
      public int i;

      {
        i = x.i + 377;
      }
    }
    ;
    threwIt = false;
    try {
      C c = new C();
    } catch (IntentError ie) {
      threwIt = true;
    }
    assertTrue(threwIt);
  }
}
