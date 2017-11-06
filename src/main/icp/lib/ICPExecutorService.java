package icp.lib;

import icp.core.FutureTask;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * Executor-Service with support for Tasks.
 */
public interface ICPExecutorService extends ExecutorService {

  @Override
  <T> FutureTask<T> submit(Callable<T> c);

  @Override
  <T> FutureTask<T> submit(Runnable task, T result);

  @Override
  FutureTask<?> submit(Runnable task);
}
