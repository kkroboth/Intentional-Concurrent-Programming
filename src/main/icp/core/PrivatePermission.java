// $Id$

package icp.core;

import java.util.logging.Logger;

import icp.lib.Task;

/**
 * A private permission only allows the task that created the object
 * to access the object. Private permissions are set up for all newly
 * created objects. Care must be taken to be sure that the same task
 * creates the object and its initial private permission. Instances of
 * this class are immutable.
 */
final class PrivatePermission implements Permission
{
  // for logging debugging info
  private static final Logger logger = Logger.getLogger("icp.core");

  // task of creator
  private final Task task;

  // private constructor
  private PrivatePermission()
  {
    task = Task.currentTask();
  }

  // private constructor
  private PrivatePermission(Task t)
  {
    task = t;
  }

  /** Create a private permission for the calling task. A static factory
   *  method is used to allow a future optimization to only build one instance
   *  per task.
   *
   *  @return returns a permission private to the calling task
   *
   */
  public static PrivatePermission newInstance()
  {
    // no optimization for now
    return new PrivatePermission();
  }

  /** Create a private permission for a particular task. A static factory
   *  method is used to allow a future optimization to only build one instance
   *  per task.
   *
   *  @param t task to create the private permission for
   *
   *  @return a permission private to the given task
   *
   */
  public static PrivatePermission newInstance(Task t)
  {
    // no optimization for now
    return new PrivatePermission(t);
  }

  /** Validate permission for calling task to make a call.
   *
   *  @throws IntentError if the access is not allowed.
   *
   */
  @Override
  public void checkCall()
  {
    if (Task.currentTask() != task)
    {
      throw new IntentError("running task does not have access to object");
    }
  }

  /** Validate permission for calling task to get a field.
   *
   *  @throws IntentError if the access is not allowed.
   *
   */
  @Override
  public void checkGet()
  {
    if (Task.currentTask() != task)
    {
      throw new IntentError("running task does not have access to object");
    }
  }

  /** Validate permission for calling task to put a field.
   *
   *  @throws IntentError if the access is not allowed.
   *
   */
  @Override
  public void checkPut()
  {
    if (Task.currentTask() != task)
    {
      throw new IntentError("running task does not have access to object");
    }
  }
}
