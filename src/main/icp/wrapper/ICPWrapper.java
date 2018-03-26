package icp.wrapper;

/**
 * A generic wrapper for any object.
 */
public final class ICPWrapper<T> {
  private final T target;

  public static <T> ICPWrapper<T> of(T target) {
    return new ICPWrapper<>(target);
  }

  private ICPWrapper(T target) {
    this.target = target;
  }

  public T target() {
    return target;
  }
}
