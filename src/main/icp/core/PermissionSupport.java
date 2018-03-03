// $Id$

package icp.core;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

import java.lang.reflect.Field;
import java.util.logging.Logger;

/**
 * Contains static methods to manipulate permissions attached
 * to objects. This class is public because the checks inserted
 * into the bytecode call methods in this class. But library code
 * (and user code also, of course) should be discouraged from accessing
 * this class directly. Instead the methods in ICP should be used.
 */
public final class PermissionSupport {
  // for logging debugging info
  private static final Logger logger = Logger.getLogger("icp.core");

  // should not be instantiated
  private PermissionSupport() {
    throw new AssertionError("this class cannot be instantiated");
  }

  // obscured name to be used for the field added to hold the permission
  private static final String AddedPermissionFieldName = "icp$42$permissionField";

  public static Permission makePrivatePermissionFor(Object unused) {
    return Permissions.getPrivatePermission();
  }

  private static final CtClass PERMISSION_TYPE;
  private static final CtField.Initializer PRIVATE_INITIALZER;

  static {
    try {
      ClassPool pool = ClassPool.getDefault();
      // what is the meaning of this comment:
      //   get the CtClass for the type of the field, which is Object
      //   it is actually Wrapper, but I can only manipulate it via reflection
      //   as an Object
      PERMISSION_TYPE = pool.get("icp.core.Permission");
      CtClass thisClass = pool.get(PermissionSupport.class.getName());
      PRIVATE_INITIALZER = CtField.Initializer.byCall(thisClass, "makePrivatePermissionFor");

    } catch (NotFoundException e) {
      throw new ExceptionInInitializerError("class not found: " + e.getMessage());
    }
  }

  /**
   * Add the field for the permission
   *
   * @param clss CtClass object for the class to which the field should be added.
   */
  public static void addPermissionField(CtClass clss) throws NotFoundException {
    logger.fine("entering addPermissionField");

    // skip interfaces
    if (clss.isInterface()) {
      logger.fine(clss.getName() + " is interface; no permission added");
      return;
    }

    // skip classes called by external frameworks
    try {
      if (clss.getAnnotation(External.class) != null) {
        logger.fine(clss.getName() + " is external; no permission added");
        return;
      }
    } catch (ClassNotFoundException e) {
      throw new NotFoundException("class not found", e);
    }

    // see if the field already exists in the class
    // if so, it was inherited from a superclass
    CtField oldField = null;
    try {
      oldField = clss.getField(AddedPermissionFieldName);
    } catch (NotFoundException e) {
      // do nothing: oldField will remain null
    }
    if (oldField != null) {
      // field exists, but it should have the right type
      // I am using equals() so assuming CtClass objects are unique.
      CtClass t;
      try {
        t = oldField.getType();
      } catch (NotFoundException e) {
        throw new ICPInternalError("NotFoundException in addPermissionField (getType)", e);
      }
      assert t.equals(PERMISSION_TYPE);

      // field should not already exist in this class
      // it should be inherited from a superclass
      assert !oldField.getDeclaringClass().equals(clss);

      logger.fine("permission field already exists in a super class");
      return;
    }

    logger.fine(String.format("adding permission field to %s", clss.getName()));
    // add the field
    CtField f;
    try {
      f = new CtField(PERMISSION_TYPE, AddedPermissionFieldName, clss);
    } catch (CannotCompileException e) {
      throw new ICPInternalError("CannotCompileException in addPermissionField", e);
    }
    try {
      clss.addField(f, PRIVATE_INITIALZER);
    } catch (CannotCompileException e) {
      throw new ICPInternalError(String.format("Cannot add field %s: %s",
        AddedPermissionFieldName, e.getMessage()), e);
    }
  }

  /**
   * Checks that a method can be called by the executing task on a given
   * object. Locates the permission for the object and calls its
   * <code>checkCall</code> method. If there is no permission, this
   * method simply returns. (In this case, the object that the method
   * is being called on is assumed to have been created by the Java core
   * classes and does not need any concurrency control.)
   *
   * @param obj object for which the method is being called
   * @throws NullPointerException if <code>obj</code> is null.
   */
  public static void checkCall(Object obj) {
    // it's our internal error if the arg is null
    // (we don't edit static method calls so obj should never be null)
    if (obj == null) {
      throw new NullPointerException();
    }

    // retrieve the permission for this this object
    Permission permission = getPermissionFieldValue(obj);

    // if there is no permission, then the object was not initialized by us
    // meaning that it must have been created by a Java core class
    // we do not check access to those objects so just return
    if (permission == null) return;

    // if there is a permission, inquire to see if it allows method call
    permission.checkCall(obj);
  }

  /**
   * Checks that a field can be read by the executing task on a given
   * object. Locates the permission for the object and calls its
   * <code>checkGet</code> method. If there is no permission, this
   * method simply returns. (In this case, the object that the method
   * is being called on is assumed to have been created by the Java core
   * classes and does not need any concurrency control.)
   *
   * @param obj object for which the method is being called
   * @throws NullPointerException if <code>obj</code> is null.
   */
  public static void checkGetField(Object obj) {
    // no check for static fields for now
    if (obj == null) return;

    // retrieve the permission for this this object
    Permission permission = getPermissionFieldValue(obj);

    // if there is no permission, then the object was not initialized by us
    // meaning that it must have been created by a Java core class
    // we do not check access to those objects so just return
    if (permission == null) return;

    // if there is a permission, inquire to see if it allows reading a field
    permission.checkGet(obj);
  }

  /**
   * Checks that a field can be written by the executing task on a given
   * object. Locates the permission for the object and calls its
   * <code>checkPut</code> method. If there is no permission, this
   * method simply returns. (In this case, the object that the method
   * is being called on is assumed to have been created by the Java core
   * classes and does not need any concurrency control.)
   *
   * @param obj object for which the method is being called
   * @throws NullPointerException if <code>obj</code> is null.
   */
  public static void checkPutField(Object obj) {
    // no check for static fields for now
    if (obj == null) return;

    // retrieve the permission for this this object
    Permission permission = getPermissionFieldValue(obj);

    // if there is no permission, then the object was not initialized by us
    // meaning that it must have been created by a Java core class
    // we do not check access to those objects so just return
    if (permission == null) return;

    // if there is a permission, inquire to see if it allows writing a field
    permission.checkPut(obj);
  }

  /**
   * EXPERIMENTAL CODE: Hook called at end of the execution of a static
   * initializer for a class. We might decide to use this hook to set
   * a permission on the class object, to allow intents to be expressed
   * for static fields and methods.
   *
   * @param clss the Class object for the class being initialized.
   */
  public static void setPermissionOnClass(Class<?> clss) {
    //System.err.println("setPermissionOnClass called for "+ clss);
  }

  //
  // static package-private methods to manipulate permissions on objects
  //

  /**
   * Retrieve the permission attached to an object.  This is used internally to implement
   * "same as" permissions.
   *
   * @param obj the object to retrieve the permission from.
   * @return the permission attached to the object
   * @throws NullPointerException if <code>obj</code> is null, or its
   *                              permission is null.
   */
  static Permission getPermission(Object obj) {
    logger.fine(String.format("[%s] getPermission called for %s",
      Thread.currentThread(), obj.toString()));

    Permission permission = getPermissionFieldValue(obj);
    assert permission != null;

    // check that current task has permission to get the permission field
    // do we really want that check?  it's used in the "same as" scheme
    permission.checkGet(obj);

    return permission;
  }

  /**
   * Set the permission attached to an object.
   *
   * @param obj        the object to set the permission for.
   * @param permission the permission to set in the object.
   * @throws NullPointerException if <code>obj</code> is null.
   */
  static void setPermission(Object obj, Permission permission) {
    // Cannot add permission to primitive values
    Class klass = obj.getClass();
    if (klass.equals(Integer.TYPE) ||
      klass.equals(Long.TYPE) ||
      klass.equals(Boolean.TYPE) ||
      klass.equals(Float.TYPE) ||
      klass.equals(Double.TYPE) ||
      klass.equals(Short.TYPE) ||
      klass.equals(Byte.TYPE) ||
      klass.equals(Character.TYPE)) {
      throw new ICPInternalError("Cannot set permission on autoboxed primitive type");
    }


    logger.fine(String.format("[%s] setPermission called for %s with %s",
      Thread.currentThread(), obj, permission.toString()));

    // need to retrieve the permission to be replaced in order to
    // check that the calling task has permission to re-set the
    // permission
    Permission oldPermission = getPermissionFieldValue(obj);
    assert oldPermission != null;

    // check that current task has permission to reset the permission field
    oldPermission.checkResetPermission(obj);

    setPermissionFieldValue(obj, permission);
  }

  /**
   * Same as {@link #setPermission(Object, Permission)} but ignores whether
   * the old permission is resettable.
   *
   * @param obj        the object to set permission for (forced)
   * @param permission the permission to set in object (forced)
   * @deprecated Do not use, will throw execption
   */
  static void forceSetPermission(Object obj, Permission permission) {
    if (true)
      throw new ICPInternalError("Violates Hatcher's Principle!");

    // This breaks some of our principles -- use caution.
    // Currently only used for thread joining
    logger.fine(String.format("[%s] forceSetPermission called for %s with %s",
      Thread.currentThread(), obj, permission.toString()));
    setPermissionFieldValue(obj, permission);
  }

  //
  // static private methods to do the dirty work
  //

  private static Field findPermissionField(Object obj) {
    // use standard Java reflection
    // See bug: issues.subclassnullpermission (fixed)
    // We will always return the permission field which is closest
    // up in inheritance chain.
    Class<?> c = obj.getClass();
    Field basePermissionField = null;

    // TODO: Should we allow @External class's set permissions directly?
    if (obj.getClass().getAnnotation(External.class) != null) {
      // Raise exception, log warning, what to do...
      // For now, raise an IntentError
      throw new IntentError("Cannot set or get permission on an @External class: " +
        obj.getClass());
    }

    do {
      try {
        basePermissionField = c.getDeclaredField(AddedPermissionFieldName);
      } catch (NoSuchFieldException | SecurityException e) {
        // doesn't exist in current class
        // no-op
      } finally {
        // Must continue up chain until base class's permission is found, or latest non-null
        c = c.getSuperclass();
      }
    } while (c != null);

    return basePermissionField;
  }

  /**
   * Retrieve the value of the field that holds the permission.
   *
   * @param obj the object whose permission field value should be retrieved.
   * @return the permission for the given object, or null if the field has not
   * been initialized, or the permission field does not exist (i.e.
   * the object is an instance of a core Java class that ICP
   * does not edit)
   */
  private static Permission getPermissionFieldValue(Object obj) {
    Field f = findPermissionField(obj);
    if (f == null) {
      // objects with no permission field are considered permanently thread-safe
      logger.fine("permission field is not present");
      return Permissions.getPermanentlyThreadSafePermission();
    }
    try {
      f.setAccessible(true);
      return (Permission) f.get(obj);
    } catch (IllegalAccessException e) {
      logger.fine("exception while getting permission field: " + e);
      throw new ICPInternalError("get of permission field failed", e);
    }
  }

  /**
   * Initialize the value of the field that holds the permission to a
   * private permission.
   *
   * @param obj the object whose permission field value should be set.
   */
  private static void initPermissionFieldValue(Object obj) {
    logger.fine(String.format("[%s] initPermission called for %s",
      Thread.currentThread(), obj));

    Permission perm = Permissions.getPrivatePermission();
    setPermissionFieldValue(obj, perm);
  }

  /**
   * Set the value of the permission field
   *
   * @param obj        the object whose permission field value should be set.
   * @param permission the permission to place in the object.
   */
  private static void setPermissionFieldValue(Object obj,
                                              Permission permission) {
    logger.fine(String.format("setting permission '%s' on object '%s'", permission, obj));

    Field f = findPermissionField(obj);
    assert f != null;
    try {
      f.setAccessible(true);
      f.set(obj, permission);
    } catch (IllegalAccessException e) {
      logger.fine("exception while setting permission field: " + e);
      throw new ICPInternalError("set of permission field failed", e);
    }
  }

}

