package applications.futures;

import applications.futures.shared.UnsafeDataList;
import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.SimpleReentrantLock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

// Future implementation 2
public class ComplexFuture2 extends Suite {
  // protects results
  private SimpleReentrantLock lock;
  private UnsafeDataList data;

  ComplexFuture2() {
    lock = new SimpleReentrantLock();
    data = new UnsafeDataList();
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
    ICP.setPermission(data, lock.getLockedPermission());
  }


  List<Integer> start(int nbFutures) throws InterruptedException {
    for (int i = 0; i < nbFutures; i++) {
      executorService.submit(Task.ofThreadSafe(() -> {
        lock.lock();

        try {
          data.add(1);
        } finally {
          lock.unlock();
        }
      }));
    }

    executorService.shutdown();
    executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

    List<Integer> results;
    lock.lock();
    try {
      results = new ArrayList<>(data.getList());
    } finally {
      lock.unlock();
    }

    return results;
  }

  public static void main(String[] args) throws InterruptedException {
    int nbFutures = 100;

    List<Integer> results = new ComplexFuture2().start(nbFutures);
    int sum = 0;
    for (Integer result : results) {
      sum += result;
    }

    assert sum == nbFutures;
  }

}
