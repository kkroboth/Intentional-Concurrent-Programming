// $Id$
//
// simple test for Javassist bytecode editing
// 
// log file will trace the loading and the editing of the classes
//

import icp.core.ICP;

public class TestApp1
{
  public static void main(String[] args)
  {
    // first initialize ICP
    ICP.initialize();

    assert(args.length == 3);
    assert(args[0].equals("abc"));
    assert(args[1].equals("def"));
    assert(args[2].equals("ghi"));
System.out.println("Hey!");
    TestClass a = new TestClass(1066);
    int x = a.getX();
    assert(x == 1066);
  }
}
