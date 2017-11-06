package applications.futures;

import icp.core.CallableTask;
import icp.core.ICP;
import icp.core.Permissions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SimpleFuture extends Suite {

  static class Result {
    int value;

    Result(int value) {
      this.value = value;
    }
  }


  void start() throws ExecutionException, InterruptedException {
    Future<Result> future = executorService.submit(CallableTask.ofThreadSafe(() -> {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      Result result = new Result(42);
      ICP.setPermission(result, Permissions.getTransferPermission());
      return result;
    }));

    assert future.get().value == 42;
  }

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    new SimpleFuture().start();
  }
}
