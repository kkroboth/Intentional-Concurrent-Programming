// $Id$
//
// simple test for javassist bytecode editing
//

public class TestClass
{
  private int x;
  public int y;

  public TestClass(int x)
  {
    this.x = x;
    this.y = 0;
  }

  public int getX()
  {
    return x;
  }
}

