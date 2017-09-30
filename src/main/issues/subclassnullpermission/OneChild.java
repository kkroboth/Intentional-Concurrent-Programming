package issues.subclassnullpermission;

import icp.core.ICP;
import icp.core.Permissions;
import issues.Misc;

public class OneChild {


  public static void main(String[] args) throws ClassNotFoundException {
    Misc.setupConsoleLogger();
    if (args.length == 0) {
      throw new RuntimeException("Usage: Test ['parent', 'child']");
    }
    String loadFirst = args[0].toLowerCase();

    // Trigger class load
    switch (loadFirst) {
      case "parent":
        Class.forName("issues.subclassnullpermission.OneChild$Parent");
        break;
      case "child":
        Class.forName("issues.subclassnullpermission.OneChild$Child");
        break;
      default:
        throw new RuntimeException("invalid option");
    }

    // Code which fails depending on order of class loading.
    new Child();
  }

  static class Parent {
    Parent() {
      ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
    }
  }

  static class Child extends Parent {

    Child() {
      //ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
    }

  }
}
