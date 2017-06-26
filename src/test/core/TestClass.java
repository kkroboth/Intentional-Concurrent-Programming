// $Id$
package core;

public class TestClass {
  public int x;

  public void justCall() {
  }

  public int callAndRead() {
    return x;
  }

  public void callAndWrite(int x) {
    this.x = x;
  }
}

