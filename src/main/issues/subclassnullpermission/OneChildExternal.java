package issues.subclassnullpermission;

import icp.core.External;
import icp.core.ICP;
import icp.core.Permissions;
import issues.Misc;

public class OneChildExternal {


  public static void main(String[] args) throws ClassNotFoundException {
    Misc.setupConsoleLogger();
    if (args.length == 0) {
      throw new RuntimeException("Usage: Test ['parent', 'child']");
    }
    String loadFirst = args[0].toLowerCase();

    // Trigger class load
    switch (loadFirst) {
      case "parent":
        Class.forName("issues.subclassnullpermission.OneChildExternal$Parent");
        break;
      case "child":
        Class.forName("issues.subclassnullpermission.OneChildExternal$Child");
        break;
      default:
        throw new RuntimeException("invalid option");
    }

    // Code which fails depending on order of class loading.
    new Child();
  }

  @External
  static class Parent {
    Parent() {
      ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
    }
  }

  static class Child extends Parent {

  }
}
