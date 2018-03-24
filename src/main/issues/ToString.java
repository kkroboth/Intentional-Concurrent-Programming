package issues;

import icp.core.ICP;
import icp.core.Permissions;

/**
 * Overriding toString() will Stackoverflow as the IntentError messages includes
 * the object in message calling toString() again.
 */
public class ToString {

  {
    ICP.setPermission(this, Permissions.getNoAccessPermission());
  }

  @Override
  public String toString() {
    return "Custom to string message";
  }

  public static void main(String[] args) {
    ToString app = new ToString();
    System.out.println(app);
  }
}
