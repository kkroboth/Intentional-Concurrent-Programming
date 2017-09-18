package lib;

import icp.core.IntentError;
import icp.lib.OneTimeLatchRegistration;
import org.testng.annotations.Test;
import util.ICPTest;

import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertThrows;

public class TestOneTimeLatchRegistraion extends ICPTest {
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

}
