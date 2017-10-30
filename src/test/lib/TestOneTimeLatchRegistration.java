package lib;

import icp.core.IntentError;
import icp.lib.OneTimeLatchRegistration;
import org.testng.annotations.Test;
import util.ICPTest;

import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertThrows;

public class TestOneTimeLatchRegistration extends ICPTest {
  static class Target {
    final AtomicInteger count = new AtomicInteger();

    void call() {
      // no op
    }

    void increment() {
      count.getAndIncrement();
    }

    int getCount() {
      return count.get();
    }
  }

  @Test(description = "opener must register itself before calling open()")
  public void mustRegisterOpenerBeforeCallingOpen() {
    OneTimeLatchRegistration latch = new OneTimeLatchRegistration();
    assertThrows(IntentError.class, latch::open);
  }

  @Test(description = "waiter must register itself before calling await()")
  public void mustRegisterWaiterBeforeCallingAwait() {
    OneTimeLatchRegistration latch = new OneTimeLatchRegistration();
    assertThrows(IntentError.class, latch::await);
  }

  @Test(description = "Opener cannot be a waiter")
  public void openerCannotBeWaiter() {
    OneTimeLatchRegistration latch = new OneTimeLatchRegistration();
    latch.registerOpener();
    assertThrows(IntentError.class, latch::registerWaiter);
  }

  @Test(description = "Waiter cannot be a opener")
  public void waiterCannotBeOpener() {
    OneTimeLatchRegistration latch = new OneTimeLatchRegistration();
    latch.registerWaiter();
    assertThrows(IntentError.class, latch::registerOpener);
  }

  @Test(description = "Opener cannot call await")
  public void waiterCannotCallAwait() {
    OneTimeLatchRegistration latch = new OneTimeLatchRegistration();
    latch.registerOpener();
    assertThrows(IntentError.class, latch::await);
  }

  @Test(description = "Waiter cannot call open")
  public void waiterCannotCallOpen() {
    OneTimeLatchRegistration latch = new OneTimeLatchRegistration();
    latch.registerWaiter();
    assertThrows(IntentError.class, latch::open);
  }

  // Note: This is now allowed
  @Test(enabled = false, description = "Cannot open twice")
  public void cannotOpenTwice() {
    OneTimeLatchRegistration latch = new OneTimeLatchRegistration();
    latch.registerOpener();
    latch.open();
    assertThrows(IntentError.class, latch::open);
  }

}
