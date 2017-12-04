package icp.wrapper;

public final class ICPArray<T> {

  public static <T> ICPArray of(T[] arr) {
    ICPArray a = new ICPArray();
    a.arr = arr;
    return a;
  }

  public T[] arr;
}
