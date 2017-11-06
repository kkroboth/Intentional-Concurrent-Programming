package applications.futures;

import applications.futures.shared.Result;
import icp.core.FutureTask;
import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permissions;
import icp.core.Task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;

// Future Implementation 3
// Wrap RunnableFuture in a Task runnable
public class SimpleFuture4 extends Suite {

  void start() throws ExecutionException, InterruptedException {
    RunnableFuture<Result> future = new FutureTask<>(() -> {
      Result result = new Result(42);
      ICP.setPermission(result, Permissions.getTransferPermission());
      return result;
    });

    executorService.execute(future);
    assert future.get().getValue() == 42;

    // Assert multiple gets fails when future is private to master task
    executorService.execute(Task.ofThreadSafe(() -> {
      try {
        future.get();
        throw new AssertionError("get() should be private");
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      } catch (IntentError good) {
      }
    }));
  }

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    SimpleFuture4 app = new SimpleFuture4();
    app.start();
    app.shutdown();
  }
}
