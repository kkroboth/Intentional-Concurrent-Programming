package applications.permissions;


import icp.core.ICP;
import icp.core.InheritPermission;
import icp.core.IntentError;
import icp.core.Permissions;
import icp.core.Task;
import icp.wrapper.ICPProxy;

import java.util.ArrayList;
import java.util.List;

public class InheritPermissionTest {

  static class A {

    @InheritPermission
    private final B mutableField = new B();
  }


  static class B {
    int data;

    @InheritPermission
    List<Integer> mutableList;

    @InheritPermission
    final C mutableField = new C();

    {
      mutableField.parent = this;
      //noinspection unchecked
      mutableList = ICPProxy.newPrivateInstance(List.class, new ArrayList());
    }
  }

  static class C {
    int data;

    // ICP does not check for cycles! Will be problematic if used incorrectly
    //@InheritPermission
    B parent;
  }

  public static void main(String[] args) {
    A app = new A();
    app.mutableField.data = 5;
    ICP.setPermission(app, Permissions.getFrozenPermission());

    try {
      app.mutableField.data = 10;
      throw new AssertionError("Data is mutable");
    } catch (IntentError good) {
    }

    new Thread(Task.ofThreadSafe(() -> {
      app.mutableField.mutableList.add(10);
    })).start();

    try {
      app.mutableField.mutableField.data = 10;
      throw new AssertionError("Data is mutable");
    } catch (IntentError good) {
    }
  }
}
