package applications.futures;

import applications.Utils;
import applications.futures.shared.ImmutableResult;
import applications.futures.shared.Result;
import icp.core.FutureTask;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.ICPExecutorService;
import icp.lib.ICPExecutors;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SimpleFuture5 {
  ICPExecutorService executor = ICPExecutors.newICPExecutorService(
    Executors.newCachedThreadPool(Utils.logExceptionThreadFactory()));

  ImmutableResult immutableResult = new ImmutableResult(43);

  public SimpleFuture5() {
    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  void start() throws ExecutionException, InterruptedException {
    Future<Result> future = executor.submit(() -> {
      Result result = new Result(42);
      ICP.setPermission(result, Permissions.getTransferPermission());
      return result;
    });

    assert future.get().getValue() == 42;

    // Runnable
    future = executor.submit(Task.ofThreadSafe(() -> {
      // ICP permission check in runnable
      assert immutableResult.getValue() == 43;
    }), new Result(42));

    assert future.get().getValue() == 42;

    //
    Result box = new Result(0);
    FutureTask<Result> futureRunnable = new FutureTask<>(() -> {
      assert immutableResult.getValue() == 43;
      box.setValue(43);
    }, box);
    ICP.setPermission(box, futureRunnable.getJoinPermission());
    executor.execute(futureRunnable);
    futureRunnable.join();
    assert futureRunnable.get().getValue() == 43;

    Result box2 = new Result(0);
    FutureTask<?> futureRunnable2 = new FutureTask<>(() -> {
      assert immutableResult.getValue() == 43;
      box2.setValue(43);
    }, null);
    ICP.setPermission(box2, futureRunnable2.getJoinPermission());
    executor.execute(futureRunnable2);
    futureRunnable2.join();

    assert box2.getValue() == 43;
    executor.shutdown();
  }

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    SimpleFuture5 app = new SimpleFuture5();
    app.start();
  }
}
