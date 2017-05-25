// $Id$

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(JUnitParamsRunner.class)
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

  @Test
  @Parameters({
      "-1, 1",
      "2, 4",
      "10, 100"
  })
  public void aTestWithArguments(int x, int y) throws Exception {
    assertEquals(y, x*x);
  }

  @Test
  @Parameters(method = "data")
  public void anotherTestWithArguments(String s, int l) throws Exception {
    assertEquals(l, s.length());
  }
  Object[] data() {
    return new Object[][] {
        {"foo", 3},
        {"bar", 3},
        {"foobar", 6}
    };
  }
}
