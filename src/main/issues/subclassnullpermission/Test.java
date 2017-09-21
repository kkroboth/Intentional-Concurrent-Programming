package issues.subclassnullpermission;

import issues.Misc;

/**
 * Issue when setting permissions on Parent class and loading Child class first.
 *
 * No problem: Load parent first
 * Problem: Load child first.
 */
public class Test {

  /**
   * Must turn on assertions: JVM flag "-ea"
   */
  public static void main(String[] args) throws ClassNotFoundException {
    Misc.setupConsoleLogger();
    if (args.length == 0) {
      throw new RuntimeException("Usage: Test ['parent', 'child', 'externalParent', 'externalChild']");
    }
    String loadFirst = args[0].toLowerCase();

    // Trigger class load
    switch (loadFirst) {
      case "parent":
        Class.forName("issues.subclassnullpermission.ParentExample");
        break;
      case "child":
        Class.forName("issues.subclassnullpermission.ChildExample");
        break;
      case "externalparent":
        Class.forName("issues.subclassnullpermission.ExternalParent");
        break;
      case "externalchild":
        Class.forName("issues.subclassnullpermission.ChildExample2");
        break;
      default:
        throw new RuntimeException("invalid option");
    }

    // Code which fails depending on order of class loading.

    if (loadFirst.startsWith("external"))
      new ChildExample2();
    else
      new ChildExample();
  }
}
