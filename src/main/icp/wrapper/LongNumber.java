package icp.wrapper;

public final class LongNumber {
  public long val;

  public static LongNumber of(long val) {
    return new LongNumber(val);
  }

  public static LongNumber zero() {
    return new LongNumber(0);
  }

  public static VLongNumber ofVolatile(long val) {
    return new VLongNumber(val);
  }

  public static VLongNumber zeroVolatile() {
    return new VLongNumber(0);
  }

  private LongNumber(long val) {
    this.val = val;
  }

  public static final class VLongNumber {
    public volatile long val;

    private VLongNumber(long val) {
      this.val = val;
    }
  }
}
