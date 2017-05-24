// $Id$

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class DemoSuite {

  @Test
  public void aSuccessfulTest() throws Exception {
    assertEquals(4, 2 + 2);
  }

  @Test(expected = IllegalMonitorStateException.class)
  public void anotherSuccessfulTest() throws Exception {
    wait();
  }

  @Test
  public void aFailedTest() throws Exception {
    assertSame(400, 200 + 200);
  }

  @Test(timeout = 500)
  public void aTimeoutTest() throws Exception {
    synchronized (this) {
      wait();
    }
  }
}
