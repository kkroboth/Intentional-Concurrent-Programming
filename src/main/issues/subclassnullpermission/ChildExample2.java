package issues.subclassnullpermission;

import icp.core.ICP;
import icp.core.Permissions;

public class ChildExample2 extends ExternalParent {

  ChildExample2() {
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
  }

}
