package icp.wrapper;

public final class Number {
  public int val;

  public static Number of(int val) {
    return new Number(val);
  }

  public static Number zero() {
    return new Number(0);
  }

  private Number(int val) {
    // use static methods
    this.val = val;
  }
}
