package application.forkjoin;

public interface Worker<T, V> {

  V execute(T job);

}
