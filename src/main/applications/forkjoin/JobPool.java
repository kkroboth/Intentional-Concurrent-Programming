package applications.forkjoin;

public interface JobPool<T> {

  /**
   * Retrieve next job in pool or return null if none exist.
   *
   * @return next job or null
   */
  T nextJob();

}
