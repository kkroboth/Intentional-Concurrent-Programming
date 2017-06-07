// $Id: PrivatePermission.java 11 2017-06-07 19:39:32Z pjh $

package icp.core;

import java.util.logging.Logger;

import icp.lib.Task;

/**
 * This permission will cause all operations to fail. This was created
 * to support testing of the core. The class needs to be public in
 * order to be used in tests located outside the package.
 */
final public class AlwaysFailsPermission implements Permission
{
  // for logging debugging info
  private static final Logger logger = Logger.getLogger("icp.core");

  private AlwaysFailsPermission() { }

  public static AlwaysFailsPermission newInstance()
  {
    return new AlwaysFailsPermission();
  }

  @Override
  public void checkCall()
  {
    throw new IntentError("always fails");
  }

  @Override
  public void checkGet()
  {
    throw new IntentError("always fails");
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

