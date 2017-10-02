package application.forkjoin;

public interface ForkJoinProvider<T, V> {

  JobPool<T> getJobPool();

  // Right now used to implement synchronizers for waiting
  void jobCompleted(V job);

}
