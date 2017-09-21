package issues.subclassnullpermission;

import icp.core.ICP;
import icp.core.Permissions;

public class ParentExample {

  ParentExample() {
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
  }
}
