package icp.lib;

import icp.core.IntentError;

/**
 * Thread class that extends java.lang.Thread.
 *
 * Gives limited access to constructors. Only constructors that take
 * a Runnable target as a parameter are visible. Sub-classing is not
 * permitted.
 *
 * Used to bootstrap individual threads.
 */
public final class Thread extends java.lang.Thread {

    /**
     * Allocates a new {@code Thread} object. This constructor has the same
     * effect as {@code Thread(ThreadGroup,Runnable,String)}
     * {@code (null, target, gname)}, where {@code gname} is a newly generated
     * name. Automatically generated names are of the form
     * {@code "Thread-"+}<i>n</i>, where <i>n</i> is an integer.
     *
     * @param target the object whose {@code run} method is invoked when this
     * thread is started. If {@code null}, this classes {@code run} method does
     * nothing.
     */
    public Thread(Runnable target) {
        super(target);
    }

    /**
     * Allocates a new {@code Thread} object. This constructor has the same
     * effect as {@code Thread(ThreadGroup,Runnable,String)}
     * {@code (group, target, gname)} ,where {@code gname} is a newly generated
     * name. Automatically generated names are of the form
     * {@code "Thread-"+}<i>n</i>, where <i>n</i> is an integer.
     *
     * @param group  the thread group. If {@code null} and there is a security
     * manager, the group is determined by {@linkplain
     * SecurityManager#getThreadGroup SecurityManager.getThreadGroup()}.
     * If there is not a security manager or {@code
     * SecurityManager.getThreadGroup()} returns {@code null}, the group
     * is set to the current thread's thread group.
     *
     * @param target the object whose {@code run} method is invoked when this
     * thread is started. If {@code null}, this thread's run method is invoked.
     *
     * @throws SecurityException if the current thread cannot create a thread
     * in the specified thread group.
     */
    public Thread(ThreadGroup group, Runnable target) {
        super(group, target);
    }

    /**
     * Allocates a new {@code Thread} object. This constructor has the same
     * effect as {@code Thread(ThreadGroup,Runnable,String)}
     * {@code (null, target, name)}.
     *
     * @param target the object whose {@code run} method is invoked when this
     * thread is started. If {@code null}, this thread's run method is invoked.
     *
     * @param name the name of the new thread
     */
    public Thread(Runnable target, String name) {
        super(target, name);
    }

    /**
     * Bootstrap the current thread, then run its target.
     */
    @Override
    public void run() {
        final Task toRtn = Task.CURRENT_TASK.get();
        if(toRtn != null) {
            throw new IntentError("Run method called directly on Thread");
        }
        Task.CURRENT_TASK.set(Task.getFirstInitialTask());
        super.run();
    }
}
