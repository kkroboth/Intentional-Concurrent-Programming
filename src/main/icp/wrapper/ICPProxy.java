package icp.wrapper;

import icp.core.PermissionSupport;
import javassist.ClassPool;
import javassist.NotFoundException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class ICPProxy {

  @SuppressWarnings("unchecked")
  public static <T> T newInstance(Class<? extends T> singleInterface, T target) {
    T instance = (T) Proxy.newProxyInstance(ICPProxy.class.getClassLoader(),
      new Class<?>[]{singleInterface}, new PermissionInvocationHandler(target));
    System.out.println(instance.getClass());
    System.out.println(instance.getClass().toString());
    System.out.println(instance.getClass().toGenericString());
    try {
      System.out.println(ClassPool.getDefault().get(instance.getClass().toString()));
    } catch (NotFoundException e) {
      e.printStackTrace();
    }
    return instance;
  }

  private static class PermissionInvocationHandler implements InvocationHandler {

    private final Object target;

    PermissionInvocationHandler(Object target) {
      this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      PermissionSupport.checkCall(target);
      return method.invoke(target, args);
    }
  }
}
