package applications.futures.shared;


import icp.core.ICP;
import icp.core.Permissions;

public class ImmutableResult {
  private final int value;

  public ImmutableResult(int value) {
    this.value = value;
    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  public int getValue() {
    return value;
  }
}
