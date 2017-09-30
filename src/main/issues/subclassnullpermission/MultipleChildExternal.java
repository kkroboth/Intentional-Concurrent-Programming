package issues.subclassnullpermission;

import icp.core.External;
import icp.core.ICP;
import icp.core.Permissions;
import issues.Misc;

public class MultipleChildExternal {


  public static void main(String[] args) throws ClassNotFoundException {
    Misc.setupConsoleLogger();
    if (args.length != 2) {
      throw new RuntimeException("Usage: Test [loadClassName] [instanceClassName]");
    }
    String loadFirst = args[0].toLowerCase();
    String instance = args[1].toLowerCase();

    // Trigger class load
    switch (loadFirst) {
      case "parent":
        Class.forName("issues.subclassnullpermission.MultipleChildExternal$Parent");
        break;
      case "child":
        Class.forName("issues.subclassnullpermission.MultipleChildExternal$Child");
        break;
      case "child2":
        Class.forName("issues.subclassnullpermission.MultipleChildExternal$Child2");
        break;
      case "child3":
        Class.forName("issues.subclassnullpermission.MultipleChildExternal$Child3");
        break;
      default:
        throw new RuntimeException("invalid option");
    }


    // Code which fails depending on order of class loading.
    switch (instance) {
      case "parent":
        new Parent();
        break;
      case "child":
        new Child();
        break;
      case "child2":
        new Child2();
        break;
      case "child3":
        new Child3();
        break;
      default:
        throw new RuntimeException("invalid instance class");
    }
  }

  static class Parent {
    Parent() {
      ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
    }
  }

  @External
  static class Child extends Parent {

  }

  static class Child2 extends Child {
    Child2() {
      ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
    }
  }

  static class Child3 extends Child2 {
  }
}
