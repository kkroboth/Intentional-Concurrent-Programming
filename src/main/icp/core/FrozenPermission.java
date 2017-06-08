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

  /**
   *  Return an instance of FrozenPermission.
   *
   *  @return FrozenPermission instance.
   */
  public static FrozenPermission newInstance()
  {
    return new FrozenPermission();
  }

  /**
   * Always succeeds.
   */
  @Override
  public void checkCall()
  {
  }

  /**
   * Always succeeds.
   */
  @Override
  public void checkGet()
  {
  }

  /**
   * Always throws an intent error.
   */
  @Override
  public void checkPut()
  {
    throw new IntentError("cannot put to frozen object");
  }

  /**
   * Always throws an intent error.
   */
  @Override
  public void checkResetPermission()
  {
    throw new IntentError("cannot reset permission of frozen object");
  }
}

