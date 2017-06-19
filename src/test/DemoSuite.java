// $Id$

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

public class DemoSuite {

  @Test
  public void aSuccessfulTest() throws Exception {
    //System.out.println(getClass().getClassLoader());
    System.out.println(System.getProperty("java.system.class.loader"));
    System.out.println(System.getProperty("java.io.tmpdir"));
    assertEquals(4, 2 + 2);
  }

  @Test(expectedExceptions = IllegalMonitorStateException.class)
  public void anotherSuccessfulTest() throws Exception {
    wait();
  }

  @Test
  public void aFailedTest() throws Exception {
    assertSame(400, 200 + 200);
  }

  @Test(timeOut = 500)
  public void aTimeoutTest() throws Exception {
    synchronized (this) {
      wait();
    }
  }

  @Test(dataProvider = "data")
  public void anotherTestWithArguments(String s, int l) throws Exception {
    assertEquals(l, s.length());
  }

  @DataProvider
  static Object[][] data() {
    return new Object[][] {
        {"foo", 3},
        {"bar", 3},
        {"foobar", 6}
    };
  }
}
