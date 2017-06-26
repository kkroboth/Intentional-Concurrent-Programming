// $Id$

package icp.core;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/* Right now, this class doesn't make sense.  As a permission-private class, all calls to methods
 * should be checked (which can be done by overriding for all the non-final ones.
 * Before embarking on this, we need to decide if we really need an ICP thread class and, if we do,
 * whether this class should extend java.lang.Thread or not.
 */

/**
 * Thread class that extends java.lang.Thread.
 *
 * Gives limited access to constructors. Only constructors that take task are allowed.
 *
 * <em>Permissions:</em> instances of this class are private to the caller
 *
 * @see Task
 */
public final class Thread extends java.lang.Thread {

  private static final Logger logger = Logger.getLogger("icp.lib");

  private static final AtomicInteger nextID = new AtomicInteger();

  private static final String BASE_NAME = "ICPThread-";

  Permission icp$42$permissionField = Permissions.getPrivatePermission();

  public Thread(ThreadGroup group, Task target, String name, long stackSize) {
    super(group, target, name, stackSize);
  }

  public Thread(ThreadGroup group, Task target, String name) {
    this(group, target, name, 0);
  }

  /**
   * Allocates a new {@code Thread} object. This constructor has the same
   * effect as {@code Thread(ThreadGroup,Runnable,String)}
   * {@code (null, target, gname)}, where {@code gname} is a newly generated
   * name. Automatically generated names are of the form
   * {@code "Thread-"+}<i>n</i>, where <i>n</i> is an integer.
   *
   * @param target the object whose {@code run} method is invoked when this thread is started. If
   *               {@code null}, this classes {@code run} method does nothing.
   */
  public Thread(Task target) {
    this(null, target);
  }

  /**
   * Allocates a new {@code Thread} object. This constructor has the same
   * effect as {@code Thread(ThreadGroup,Runnable,String)}
   * {@code (group, target, gname)} ,where {@code gname} is a newly generated
   * name. Automatically generated names are of the form
   * {@code "Thread-"+}<i>n</i>, where <i>n</i> is an integer.
   *
   * @param group  the thread group. If {@code null} and there is a security manager, the group is
   *               determined by {@linkplain SecurityManager#getThreadGroup
   *               SecurityManager.getThreadGroup()}. If there is not a security manager or {@code
   *               SecurityManager.getThreadGroup()} returns {@code null}, the group is set to the
   *               current thread's thread group.
   * @param target the object whose {@code run} method is invoked when this thread is started. If
   *               {@code null}, this thread's run method is invoked.
   * @throws SecurityException if the current thread cannot create a thread in the specified thread
   *                           group.
   */
  public Thread(ThreadGroup group, Task target) {
    this(group, target, BASE_NAME + nextID.incrementAndGet());
  }

  /**
   * Allocates a new {@code Thread} object. This constructor has the same
   * effect as {@code Thread(ThreadGroup,Runnable,String)}
   * {@code (null, target, name)}.
   *
   * @param target the object whose {@code run} method is invoked when this thread is started. If
   *               {@code null}, this thread's run method is invoked.
   * @param name   the name of the new thread
   */
  public Thread(Task target, String name) {
    this(null, target, name);
  }
}
