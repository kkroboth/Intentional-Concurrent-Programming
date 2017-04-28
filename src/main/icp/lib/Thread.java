// $Id$

package icp.lib;

import javassist.ClassPool;

public class Thread {

  public Thread() {
    ClassPool cp = ClassPool.getDefault();
    System.out.println(cp);
  }
}