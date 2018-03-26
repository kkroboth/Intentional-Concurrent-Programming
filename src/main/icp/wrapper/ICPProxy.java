package icp.wrapper;

import icp.core.ICP;
import icp.core.Permission;
import icp.core.PermissionSupport;
import icp.core.Permissions;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.BiConsumer;

public final class ICPProxy {

  @SuppressWarnings("unchecked")
  public static <T> T newInstance(Class<? extends T> singleInterface, T target,
                                  BiConsumer<Object, Object> permissionApplier) {
    PermissionTarget permissionTarget = new PermissionTarget();
    T proxy = (T) Proxy.newProxyInstance(ICPProxy.class.getClassLoader(),
      new Class<?>[]{singleInterface, ProxyInfo.class},
      new PermissionInvocationHandler(target, permissionTarget));
    permissionApplier.accept(permissionTarget, proxy);
    return proxy;
  }

  @SuppressWarnings("unchecked")
  public static <T> T newPrivateInstance(Class<? extends T> singleInterface, T target) {
    return newInstance(singleInterface, target, (i, j) -> {
      // do nothing -- already private
    });
  }

  @SuppressWarnings("unchecked")
  public static <T> T newFrozenInstance(Class<? extends T> singleInterface, T target) {
    return newInstance(singleInterface, target, (permHolder, targetObj) -> ICP.setPermission(permHolder,
      Permissions.getFrozenPermission()));
  }

  @SuppressWarnings("unchecked")
  public static <T> T newThreadSafeInstance(Class<? extends T> singleInterface, T target) {
    return newInstance(singleInterface, target, (permHolder, targetObj) -> ICP.setPermission(permHolder,
      Permissions.getThreadSafePermission()));
  }

  public static void setProxyPermission(Object proxy, Permission permission) {
    if (!Proxy.isProxyClass(proxy.getClass())) {
      throw new RuntimeException("Proxy is not a java.reflect.Proxy");
    }

    // Note: The ProxyInfo class must be from ICPLoader or the loader that created the proxy class.
    // Otherwise ClassCastException will be thrown.
    try {
      //noinspection unchecked
      Class<ProxyInfo> proxyInfoClass = (Class<ProxyInfo>) Class.forName("icp.wrapper.ICPProxy$ProxyInfo", true,
        proxy.getClass().getClassLoader());
      Method method = proxyInfoClass.getMethod("ICPGetPermissionHolder");
      method.setAccessible(true);
      Object permissionHolder = method.invoke(proxy);
      ICP.setPermission(permissionHolder, permission);
    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
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
      if (method.getName().equals("ICPGetPermissionHolder")) {
        return permissionTarget;
      } else if (method.getName().equals("ICPGetObjectTarget")) {
        return target;
      }

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

  /**
   * Interface for which every proxy will implement to access the target.
   * <p>
   * Internally used to update the permission
   */
  private interface ProxyInfo {
    Object ICPGetPermissionHolder();

    Object ICPGetObjectTarget();
  }
}
