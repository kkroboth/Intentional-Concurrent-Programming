package icp.lib;

import icp.core.Permission;
import icp.core.Task;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Executor-Service with support for Tasks.
 */
public interface ICPExecutorService {

  <T> Future<T> submit(Callable<T> c);

  <T> Future<T> submit(Task task, T result);

  Future<?> submit(Task task);

  Permission getAwaitTerminationPermission();

  void shutdown();

  List<Runnable> shutdownNow();

  boolean isShutdown();

  boolean isTerminated();

  boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;

  <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException;

  <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException;

  <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException;

  <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

  void execute(Task command);
}
