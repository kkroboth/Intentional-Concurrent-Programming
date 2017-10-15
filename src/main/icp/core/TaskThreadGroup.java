package icp.core;

public final class TaskThreadGroup {
  private static final Object[] EMPTY_TARGETS = new Object[0];

  private final TaskThread[] taskThreads;
  private Object[] targets;


  public TaskThreadGroup(TaskThread[] taskThreads) {
    if (taskThreads.length == 0)
      throw new IllegalArgumentException("Must be at least one TaskThread in taskThreads");
    this.taskThreads = taskThreads;
  }

  public void start(Object... targets) {
    // NOTE: TaskThreadGroup modifies the targets, not the
    // individual task threads.
    for (int i = 0; i < taskThreads.length; i++) {
      taskThreads[i].start(EMPTY_TARGETS);
    }

    this.targets = targets;
  }

  public void join() throws InterruptedException {
    for (TaskThread taskThread : taskThreads) {
      taskThread.thread.join();
    }

    // Set all target permissions to private
    for (Object target : targets) {
      PermissionSupport.forceSetPermission(target, Permissions.getPrivatePermission());
    }

  }
}
