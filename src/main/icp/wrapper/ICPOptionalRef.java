package icp.wrapper;

public final class ICPOptionalRef<T> {
  public T val;

  public static <T> ICPOptionalRef<T> of(T val) {
    return new ICPOptionalRef<>(val);
  }

  public static <T> ICPOptionalRef<T> empty() {
    return new ICPOptionalRef<>(null);
  }

  private ICPOptionalRef(T val) {
    this.val = val;
  }
}
