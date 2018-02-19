package icp.wrapper;

import icp.core.ICP;
import icp.core.PermissionSupport;
import icp.core.Permissions;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.BiConsumer;

public final class ICPProxy {

  @SuppressWarnings("unchecked")
  public static <T> T newInstance(Class<? extends T> singleInterface, T target,
                                  BiConsumer<Object, Object> permissionApplier) {
    PermissionTarget permissionTarget = new PermissionTarget();
    T proxy = (T) Proxy.newProxyInstance(ICPProxy.class.getClassLoader(),
      new Class<?>[]{singleInterface}, new PermissionInvocationHandler(target, permissionTarget));
    permissionApplier.accept(permissionTarget, proxy);
    return proxy;
  }

  private static class PermissionInvocationHandler implements InvocationHandler {
    private final Object target;
    private final Object permissionTarget;

    PermissionInvocationHandler(Object target, Object permissionTarget) {
      this.target = target;
      this.permissionTarget = permissionTarget;
      ICP.setPermission(this, Permissions.getFrozenPermission());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      PermissionSupport.checkCall(permissionTarget);
      return method.invoke(target, args);
    }
  }

  /**
   * The returned proxy object does not allow permissions to be set on it. This
   * class is used only for permission setting.
   */
  private static class PermissionTarget {

  }
}
