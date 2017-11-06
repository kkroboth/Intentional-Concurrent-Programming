package applications.futures;

import applications.futures.shared.Result;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

// Future Implementation 2
// Wrap RunnableFuture in a Task runnable
public class SimpleFuture3 extends Suite {

  void start() throws ExecutionException, InterruptedException {
    RunnableFuture<Result> future = new FutureTask<>(() -> {
      Result result = new Result(42);
      ICP.setPermission(result, Permissions.getTransferPermission());
      return result;
    });

    executorService.execute(Task.ofThreadSafe(future));
    assert future.get().getValue() == 42;
  }

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    SimpleFuture3 app = new SimpleFuture3();
    app.start();
    app.shutdown();
  }
}
