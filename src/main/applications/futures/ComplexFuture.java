package applications.futures;

import icp.core.ICP;
import icp.core.Permissions;
import icp.core.Task;
import icp.lib.SimpleReentrantLock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ComplexFuture {
  // protects results
  private SimpleReentrantLock lock;
  private ExecutorService pool;
  private Data data;

  // guarded by "lock"
  class Data {
    private ArrayList<Integer> results = new ArrayList<>();
  }

  ComplexFuture() {
    lock = new SimpleReentrantLock();
    data = new Data();
    pool = Executors.newCachedThreadPool();
    ICP.setPermission(this, Permissions.getPermanentlyThreadSafePermission());
    ICP.setPermission(data, lock.getLockedPermission());
  }


  List<Integer> start(int nbFutures) throws InterruptedException {
    for (int i = 0; i < nbFutures; i++) {
      pool.submit(Task.fromThreadSafeRunnable(() -> {
        lock.lock();

        try {
          data.results.add(1);
        } finally {
          lock.unlock();
        }
      }));
    }

    pool.shutdown();
    pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

    List<Integer> results;
    lock.lock();
    try {
      results = new ArrayList<>(data.results);
    } finally {
      lock.unlock();
    }

    return results;
  }

  public static void main(String[] args) throws InterruptedException {
    int nbFutures = 100;

    List<Integer> results = new ComplexFuture().start(nbFutures);
    int sum = 0;
    for (Integer result : results) {
      sum += result;
    }

    assert sum == nbFutures;
  }

}
