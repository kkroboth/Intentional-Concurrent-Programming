package applications.futures;

import icp.core.CallableTask;
import icp.core.ICP;
import icp.core.Permissions;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class SimpleFuture2 {
  private ArrayList<String> list = new ArrayList<>();

  static class Result {
    int data = 0;
  }

  {
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
  }

  void start() throws ExecutionException, InterruptedException {
    Future<Result> future = ForkJoinPool.commonPool().submit(CallableTask.ofThreadSafe(() -> {
      list.add("Hello World");
      Result result = new Result();
      result.data = 42;
      ICP.setPermission(result, Permissions.getTransferPermission());
      return result;
    }));

    Result result = future.get();
    assert list.get(0).equals("Hello World");
    assert result.data == 42;
  }

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    new SimpleFuture2().start();
  }
}
