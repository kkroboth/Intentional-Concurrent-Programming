package applications.permissions;

import icp.core.ICP;
import icp.core.IntentError;
import icp.core.Permissions;

import java.util.Date;

/**
 * Treats startDate as effectively immutable.
 * <p>
 * getDate() returns data without copying
 * <p>
 * getCopiedDate() returns a new date with same time
 */
public class DateEffectivelyImmutable {

  private final Date startDate;

  public DateEffectivelyImmutable() {
    startDate = new MyDate(100);
    ICP.setPermission(startDate, Permissions.getFrozenPermission());
  }

  public Date getDate() {
    return startDate;
  }

  public Date getImmutableDate() {
    return new Date(startDate.getTime());
  }

  public static void main(String[] args) {
    DateEffectivelyImmutable app = new DateEffectivelyImmutable();

    Date frozen = app.getDate();
    try {
      frozen.setTime(1); // error
      throw new AssertionError("Can't edit effectively immutable object");
    } catch (IntentError e) {
      e.printStackTrace();
    }

    Date copied = app.getImmutableDate();
    copied.setTime(1); // allowed
  }

  static final class MyDate extends Date {
    boolean modFlag;

    MyDate(long time) {
      super(time);
    }

    @Override
    public void setTime(long time) {
      // Required since checkCall passes for java.util.Date.setTime(),
      // but setTime() modifies the time!
      modFlag = !modFlag;
      super.setTime(time);
    }
  }

}
