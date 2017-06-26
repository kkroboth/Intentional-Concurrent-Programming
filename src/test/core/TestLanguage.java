// $Id: DemoSuite.java 17 2017-06-19 16:59:12Z charpov $
package core;

import icp.core.DoNotEdit;
import icp.core.ICP;
import icp.core.Permissions;
import org.testng.annotations.Test;
import util.ICPTest;

import static org.testng.Assert.assertNull;
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
}
