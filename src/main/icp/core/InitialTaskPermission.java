// $Id$

package icp.core;

import icp.lib.Task;

/**
 * Special permission to allow the bootstrapping of threads.
 */
final class InitialTaskPermission implements Permission {

    private InitialTaskPermission(){}

    private static InitialTaskPermission INSTANCE =
      new InitialTaskPermission();

    public static InitialTaskPermission get(){
        return INSTANCE;
    }

    /**
     * Validate permission for put for initial task.
     *
     * @throws IntentError if initial task has already been set for the thread.
     */
    @Override
    public void checkPut() {
        // it is really an internal error if we get an IntentError here.
        // this is only exercised when null is assigned to the TaskLocalMap
        // in the Task class.
        if (Task.CURRENT_TASK.get() != null) {
          throw new IntentError("Thread " + Thread.currentThread() +
          " already has initial task");
        }
    }

    /**
     * Validate permission for get for initial task.
     *
     * @throws IntentError always because it should never be called.
     */
    @Override
    public void checkGet() {
      throw new IntentError("Thread " + Thread.currentThread() +
          " checkGet called for initial task");
    }

    /**
     * Validate permission for call for initial task.
     *
     * @throws IntentError always because it should never be called.
     */
    @Override
    public void checkCall() {
      throw new IntentError("Thread " + Thread.currentThread() +
          " checkCall called for initial task");
    }
}
