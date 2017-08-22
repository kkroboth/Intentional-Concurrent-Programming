// $Id: DemoSuite.java 17 2017-06-19 16:59:12Z charpov $
package core;

import icp.core.External;
import icp.core.IntentError;
import icp.core.Task;
import org.testng.annotations.Test;
import util.ICPTest;

import static org.testng.Assert.*;
import static util.Misc.executeInNewJavaThread;

@External
public class TestInit extends ICPTest {

  @Test(description = "the running thread is given an implicit task")
  public void testInitTask() throws Exception {
    assertNotNull(Task.currentTask());
  }

  @Test(description = "current task does not change")
  public void testCurentTaskConstant() throws Exception {
    Task current = Task.currentTask();
    assertSame(Task.currentTask(), current);
  }

  @Test(description = "non-ICP thread cannot create instrumented object after init")
  public void testNoTaskThread() throws Exception {
    // ensure a first ICP operation from the "main" thread
    new Object() {
    };
    assertTrue(executeInNewJavaThread(() -> new Object() {
    }) instanceof IntentError);
  }
}
