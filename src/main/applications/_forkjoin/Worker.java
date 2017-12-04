package applications._forkjoin;

public interface Worker<T, V> {

  V execute(T job);

}
