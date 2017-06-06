// $Id$

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import icp.core.*;
import icp.lib.*;

@RunWith(JUnitParamsRunner.class)
public class TestSimpleReentrantLock {

  @Test(expected = IntentError.class)
  public void test1() throws Exception {
    final TestClass obj = new TestClass(42);
    SimpleReentrantLock lock = SimpleReentrantLock.newInstance();
    ICP.setPermission(obj, lock.lockedPermission());
    Runnable task = () -> {
      int x = obj.getX();
    };
    new Thread(task).start();
  }

}

