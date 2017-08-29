// $Id$
//
// test nested class
//

package core;

import icp.core.ICP;
import icp.core.Permissions;

public class NestedClasses {
  // first a static nested class
  static private class Inner1 {
    public int x;

    public Inner1(int x) {
      this.x = x;
    }

    public int grabX() {
      return x;
    }
  }

  // now a non-static nested class
  private class Inner2 {
    public int y;

    public Inner2(int y) {
      this.y = y;
    }

    public int grabY() {
      return y;
    }
  }

  private Inner1 x;
  private Inner2 y;

  public NestedClasses(int x) {
    this.x = new Inner1(x);
    ICP.setPermission(this.x, Permissions.getPermanentlyThreadSafePermission());
    this.y = new Inner2(x + 1000);
    ICP.setPermission(this.y, Permissions.getPermanentlyThreadSafePermission());
  }

  public int getX() {
    return x.x;
  }

  public synchronized void incX() {
    x.x++;
  }

  public Object exposeX() {
    return x;
  }

  public int grabX() {
    return x.grabX();
  }

  public int getY() {
    return y.y;
  }

  public synchronized void incY() {
    y.y++;
  }

  public Object exposeY() {
    return y;
  }

  public int grabY() {
    return y.grabY();
  }

  public int testLocal() {
    // will only be accessed by the same thread that creates it
    class Local {
      private int z;

      public Local() {
        z = x.grabX() + y.grabY();
      }

      public int getZ() {
        return z;
      }
    }

    Local loc = new Local();
    return loc.getZ();
  }

  // bizarre use of local class
  // return instance as an Object, and accept an instance as a parameter
  public Object testLocal2(Object obj) {
    class Local {
      private int z;

      public Local() {
        z = x.grabX() + y.grabY();
      }

      public Local(int val) {
        z = x.grabX() + y.grabY() + val;
      }

      public int getZ() {
        return z;
      }
    }

    Local loc = null;
    if (obj == null) {
      loc = new Local();
    } else {
      Local old = (Local) obj;
      loc = new Local(old.getZ());
    }
    //System.out.printf("testLocal2 returning %d\n", loc.getZ());
    return loc;
  }

}

