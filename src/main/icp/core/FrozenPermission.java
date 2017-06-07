// $Id: PrivatePermission.java 11 2017-06-07 19:39:32Z pjh $

package icp.core;

import java.util.logging.Logger;

import icp.lib.Task;

/**
 * This permission allows get and call, but not put or reset.
 */
final public class FrozenPermission implements Permission
{
  // for logging debugging info
  private static final Logger logger = Logger.getLogger("icp.core");

  private FrozenPermission() { }

  public static FrozenPermission newInstance()
  {
    return new FrozenPermission();
  }

  @Override
  public void checkCall()
  {
  }

  @Override
  public void checkGet()
  {
  }

  @Override
  public void checkPut()
  {
    throw new IntentError("always fails");
  }

  @Override
  public void checkResetPermission()
  {
    throw new IntentError("always fails");
  }
}

