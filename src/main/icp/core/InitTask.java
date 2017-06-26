// $Id$

package icp.core;

/**
 * Special task used for bootstrapping.  It cannot be run because it is already running.
 * A single instance of this task is created in the first call to {@code Task.currentTask},
 * typically triggered by the creation of a user-level object by the initial thread.  It is
 * important that the creation of this task <em>does not trigger a call to</em> {@code currentTask}.
 * In particular, the permission field of this task is left to null (which represents permanently
 * thread-safe).  Note that the instance is only used as a marker (the identity of the initial
 * task) and has no useful methods or fields.
 */
class InitTask extends Task {
  @Override
  public void run() {
    throw new IntentError("task already run/running");
  }
}
