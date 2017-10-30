package applications.forkjoin;

public interface Worker<T, V> {

  V execute(T job);

}
