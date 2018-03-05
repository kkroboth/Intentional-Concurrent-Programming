package edu.unh.letsmeet.engine.function;

@FunctionalInterface
public interface CheckedConsumer<T> {

  void accept(T t) throws Exception;

}
