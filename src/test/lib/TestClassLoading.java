package lib;

import icp.core.External;
import icp.core.IntentError;
import org.testng.annotations.Test;
import util.ICPTest;

import static icp.core.ICP.setPermission;
import static icp.core.Permissions.getPermanentlyThreadSafePermission;
import static icp.core.Permissions.getThreadSafePermission;
import static javassist.runtime.DotClass.fail;
import static org.testng.Assert.assertThrows;

/**
 * These tests will fail due to assertion errors while retrieving the permission field
 * using `setPermission` method in PermissionSupport.
 */
public class TestClassLoading extends ICPTest {
  // Class Scenarios -- Documented more in tests
  // Note: Keeping all scenarios in one class and subclasses are inner static classes
  // to avoid bloating this file.

  // Set permission in parent's constructor. (non-resettable)
  static class Scenario1 {
    Scenario1() {
      setPermission(this, getPermanentlyThreadSafePermission());
    }

    static class Sub extends Scenario1 {

    }

    static class Sub2 extends Scenario1 {
      Sub2() {
        setPermission(this, getPermanentlyThreadSafePermission());
      }
    }
  }

  // Same as Scenario1, but parent has resettable permission.
  static class Scenario2 {
    Scenario2() {
      setPermission(this, getThreadSafePermission());
    }

    static class Sub extends Scenario2 {
      Sub() {
        setPermission(this, getPermanentlyThreadSafePermission());
      }
    }
  }

  // Has @External annotation in middle of inheritance chain.
  // A -> B (external) -> C
  static class Scenario3 {
    Scenario3() {
      setPermission(this, getPermanentlyThreadSafePermission());
    }

    static class A extends Scenario3 {

    }

    @External
    static class B extends A {

    }

    static class C extends B {

    }
  }

  // Scenarios uses more @External
  static class Scenario4 {

    @External
    static class ExternalParent {

    }

    static class NormChild extends ExternalParent {
      NormChild() {
        setPermission(this, getPermanentlyThreadSafePermission());
      }
    }

    @External
    static class ExternalChild extends ExternalParent {
      ExternalChild() {
        setPermission(this, getPermanentlyThreadSafePermission());
      }
    }

    @External
    static class OnlyExternalChild extends Scenario4 {
      OnlyExternalChild() {
        setPermission(this, getPermanentlyThreadSafePermission());
      }
    }
  }

  // Helper to load inner classes from this Test Class
  static void loadClass(String name) {
    try {
      Class.forName(String.format("lib.TestClassLoading$%s", name));
    } catch (ClassNotFoundException e) {
      //noinspection ThrowableNotThrown
      fail(e);
    }
  }

  @Test(description = "Scenario 1: Load parent class first")
  void scenario1Parent() {
    loadClass("Scenario1");
    new Scenario1.Sub();
  }

  @Test(description = "Scenario 1: Load child class first")
  void scenario1Child() {
    loadClass("Scenario1$Sub");
    new Scenario1.Sub();
  }

  @Test(description = "Scenario 1: Sub2 -- Can't reset permanent permission in child")
  void scenario1Sub2() {
    assertThrows(IntentError.class, Scenario1.Sub2::new);
  }

  @Test(description = "Scenario 2: Sub can reset permission")
  void scenario2ResetPerm() {
    new Scenario2.Sub();
  }

  @Test(description = "Scenario 3: Load last child first")
  void scenario3LoadLastChild() {
    loadClass("Scenario3$C");
    new Scenario3.C();
  }

  @Test(description = "Scenario 4: Parent is external, child isn't")
  void scenario4externParentNormChild() {
    new Scenario4.NormChild();
  }

  @Test(description = "Scenario 4: Both parent and child external")
  void scenario4BothExternal() {
    assertThrows(IntentError.class, Scenario4.ExternalChild::new);
  }

  // TODO: Is it right to allow an external child whose parent isn't, to set permission???
  @Test(description = "Scenario 4: Parent normal, child external")
  void scenario4ParentNormalChildExternal() {
    new Scenario4.OnlyExternalChild();
  }

}
