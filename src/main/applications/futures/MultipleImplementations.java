package applications.futures;

import applications.Utils;
import applications.futures.shared.Result;
import icp.core.CallableTask;
import icp.core.FutureTask;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.ICPExecutorService;
import icp.lib.ICPExecutors;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;

/**
 * What happens when we use all implementations of futures at once.
 */
public class MultipleImplementations extends Suite {

  void start() throws ExecutionException, InterruptedException {
    RunnableFuture<Result> future = new FutureTask<>(CallableTask.ofThreadSafe(() -> {
      Result result = new Result(42);
      ICP.setPermission(result, Permissions.getTransferPermission());
      return result;
    }));

    executorService.execute(Task.ofThreadSafe(future));
    assert future.get().getValue() == 42;

    future = new FutureTask<>(() -> {
      Result result = new Result(42);
      ICP.setPermission(result, Permissions.getTransferPermission());
      return result;
    });

    executorService.execute(Task.ofThreadSafe(future));
    assert future.get().getValue() == 42;

    // Grand Finale
    ICPExecutorService icpexecutor = ICPExecutors.newICPExecutorService(Executors.newCachedThreadPool(
      Utils.logExceptionThreadFactory()
    ));
    future = new FutureTask<>(CallableTask.ofThreadSafe(() -> {
      Result result = new Result(42);
      ICP.setPermission(result, Permissions.getTransferPermission());
      return result;
    }));

    icpexecutor.execute(Task.ofThreadSafe(future));
    assert future.get().getValue() == 42;
    icpexecutor.shutdown();
  }

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    MultipleImplementations app = new MultipleImplementations();
    app.start();
    app.shutdown();
  }
}
