// $Id$

import icp.core.External;
import icp.core.Task;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import util.ICPTest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/*
 * It is important that test classes are not instrumented.  Some features of TestNG (most notably
 * timeouts and parallel runs) rely on their own, non-ICP threads.
 *
 * However, this also means that read and writes to fields directly in the methods of this class
 * will not be checked.   Checks on method calls and reset are not an issue since they are not
 * added to the calling side.
 */
@External
public class DemoSuite extends ICPTest {

  static {
    System.out.printf("loading DemoSuite%n");
    System.out.printf("thread: %s%n", Thread.currentThread().getName());
    System.out.printf("loader: %s%n", DemoSuite.class.getClassLoader());
  }

  @Test(description = "2 + 2 = 4")
  public void aSuccessfulTest() throws Exception {
    assertEquals(4, 2 + 2);
  }

  @Test(expectedExceptions = IllegalMonitorStateException.class)
  public void anotherSuccessfulTest() throws Exception {
    wait();
  }

  @Test(dataProvider = "data")
  public void anotherTestWithArguments(String s, int l) throws Exception {
    assertEquals(l, s.length());
  }

  @DataProvider
  static Object[][] data() {
    return new Object[][]{
        {"foo", 3},
        {"bar", 3},
        {"foobar", 6}
    };
  }

  // this test should fail with timeouts below 500
  @Test(timeOut = 1000)
  public void aTestThatTimesOut() throws Exception {
    synchronized (this) {
      wait(500);
    }
  }

  @Test(description = "checking that tests are run by tasks")
  public void currentTaskInit() throws Exception {
    assertNotNull(Task.currentTask());
  }
}
