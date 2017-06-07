// $Id$

package icp.core;

import icp.lib.InitialTask;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

import javassist.*;
import javassist.expr.*;
import java.util.logging.Logger;

/**
 * Contains static methods to manipulate permissions attached
 * to objects. This class is public because the checks inserted
 * into the bytecode call methods in this class. But library code
 * (and user code also, of course) should be discouraged from accessing
 * this class directly. Instead the methods in ICP should be used.
 * 
 */
public final class PermissionSupport
{
  // for logging debugging info
  private static final Logger logger = Logger.getLogger("icp.core");

  // should not be instantiated
  private PermissionSupport() {}

  // obscured name to be used for the field added to hold the permission
  private static String AddedPermissionFieldName = "icp$42$permissionField";

  /** Add the field for the permission
   *
   *  @param clss CtClass object for the class to which the field should be
   *              added.
   */
  public static void addPermissionField(CtClass clss)
  {
    // skip interfaces
    if (clss.isInterface())
    {
      return;
    }

    // also skip abstract classes
    int mods = clss.getModifiers();
    if (javassist.Modifier.isAbstract(mods))
    {
      return;
    }

    logger.fine("adding the permission field");

    // get the CtClass for the type of the field, which is Object
    // it is actually Wrapper, but I can only manipulate it via reflection
    // as an Object
    ClassPool pool = ClassPool.getDefault();
    CtClass permissionFieldType = null;
    try {
      permissionFieldType = pool.get("java.lang.Object");
    }
    catch (NotFoundException e)
    {
      Message.fatal("NotFoundException in addPermissionField");
    }

    // see if the field already exists in the class
    // if so, it was inherited from a superclass
    CtField oldField = null;
    try {
      oldField = clss.getField(AddedPermissionFieldName);
    }
    catch (NotFoundException e)
    {
      // do nothing: oldField will remain null
    }
    if (oldField != null)
    {
      // field exists, but it should have the right type
      // I am using equals() so assuming CtClass objects are unique.
      CtClass t = null;
      try {
        t = oldField.getType();
      }
      catch (NotFoundException e)
      {
        Message.fatal("NotFoundException in addPermissionField (getType)");
      }
      assert(t.equals(permissionFieldType));

      // field should not already exist in this class
      // it should be inherited from a superclass
      assert(!oldField.getDeclaringClass().equals(clss));

      logger.fine("permission field already exists in a super class");
      return;
    }

    // add the field
    CtField f = null;
    try {
      f = new CtField(permissionFieldType, AddedPermissionFieldName, clss);
    }
    catch (CannotCompileException e)
    {
      Message.fatal("CannotCompileException in addPermissionField");
    }
    try {
      clss.addField(f);
    }
    catch (CannotCompileException e)
    {
      Message.error("Cannot add field %s: already exists?",
        AddedPermissionFieldName);
    }
  }

  /** Checks that a method can be called by the executing task on a given
   *  object. Locates the permission for the object and calls its
   *  <code>checkCall</code> method. If there is no permission, this
   *  method simply returns. (In this case, the object that the method
   *  is being called on is assumed to have been created by the Java core
   *  classes and does not need any concurrency control.)
   *
   *  @param obj object for which the method is being called
   *
   *  @throws NullPointerException if <code>obj</code> is null.
   */
  public static void checkCall(Object obj)
  {
    // it's our internal error if the arg is null
    // (we don't edit static method calls so obj should never be null)
    if (obj == null)
    {
      throw new NullPointerException();
    }

    // retrieve the permission for this this object
    Permission permission = getPermissionFieldValue(obj);

    // if there is no permission, then the object was not initialized by us
    // meaning that it must have been created by a Java core class
    // we do not check access to those objects so just return
    if (permission == null) return;

    // if there is a permission, inquire to see if it allows method call
    permission.checkCall();
  }

  /** Checks that a field can be read by the executing task on a given
   *  object. Locates the permission for the object and calls its
   *  <code>checkGet</code> method. If there is no permission, this
   *  method simply returns. (In this case, the object that the method
   *  is being called on is assumed to have been created by the Java core
   *  classes and does not need any concurrency control.)
   *
   *  @param obj object for which the method is being called
   *
   *  @throws NullPointerException if <code>obj</code> is null.
   */
  public static void checkGetField(Object obj)
  {
    // no check for static fields for now
    if (obj == null) return;

    // retrieve the permission for this this object
    Permission permission = getPermissionFieldValue(obj);

    // if there is no permission, then the object was not initialized by us
    // meaning that it must have been created by a Java core class
    // we do not check access to those objects so just return
    if (permission == null) return;

    // if there is a permission, inquire to see if it allows reading a field
    permission.checkGet();
  }

  /** Checks that a field can be written by the executing task on a given
   *  object. Locates the permission for the object and calls its
   *  <code>checkPut</code> method. If there is no permission, this
   *  method simply returns. (In this case, the object that the method
   *  is being called on is assumed to have been created by the Java core
   *  classes and does not need any concurrency control.)
   *
   *  @param obj object for which the method is being called
   *
   *  @throws NullPointerException if <code>obj</code> is null.
   */
  public static void checkPutField(Object obj)
  {
    // no check for static fields for now
    if (obj == null) return;

    // retrieve the permission for this this object
    Permission permission = getPermissionFieldValue(obj);

    // if there is no permission, then the object was not initialized by us
    // meaning that it must have been created by a Java core class
    // we do not check access to those objects so just return
    if (permission == null) return;

    // if there is a permission, inquire to see if it allows writing a field
    permission.checkPut();
  }

  /** Initialize the permission for a newly created object. The object is
   *  given a private permission, allowing only the creating task to access
   *  the object.
   *
   *  @param obj the newly created object
   *
   *  @throws NullPointerException if <code>obj</code> is null.
   */
  public static void initialize(Object obj)
  {
    // it's our internal error if it is null
    if (obj == null)
    {
      throw new NullPointerException();
    }
      
    logger.fine(String.format("[%s] permission initialize called for %s",
      Thread.currentThread(), obj.toString()));

    // is the permission field already initialized?
    // then do nothing, as this is a redundant call to initialize
    if (getPermissionFieldValue(obj) != null)
    {
      return;
    }

    // set up the initial permission
    initPermissionFieldValue(obj);
  }

  //
  // static package-private methods to manipulate permissions on objects
  //

  /** Retrieve the permission attached to an object.
   *
   *  @param obj the object to retrieve the permission from.
   *
   *  @throws NullPointerException if <code>obj</code> is null, or its
   *  permission is null.
   *
   *  @return the permission attached to the object
   */
  static Permission getPermission(Object obj)
  {
    // it's our internal error if object is null
    if (obj == null)
    {
      throw new NullPointerException();
    }

    logger.fine(String.format("[%s] getPermission called for %s",
      Thread.currentThread(), obj.toString()));

    Permission permission = getPermissionFieldValue(obj);

    // it's our internal error if permission is null
    if (permission == null)
    {
      throw new NullPointerException();
    }

    // check that current task has permission to get the permission field
    permission.checkGet();

    return permission;
  }

  /** Set the permission attached to an object.
   *
   *  @param obj the object to set the permission for.
   *  @param permission the permission to set in the object.
   *
   *  @throws NullPointerException if <code>obj</code> is null.
   */
  static void setPermission(Object obj, Permission permission)
  {
    // it's our internal error if object is null
    if (obj == null)
    {
      throw new NullPointerException();
    }

    logger.fine(String.format("[%s] setPermission called for %s with %s",
      Thread.currentThread(), obj.toString(), permission.toString()));

    // need to retrieve the permission to be replaced in order to
    // check that the calling task has permission to re-set the
    // permission
    Permission oldPermission = getPermissionFieldValue(obj);

    // it's our internal error if permission is null
    if (oldPermission == null)
    {
      throw new NullPointerException();
    }

    // check that current task has permission to reset the permission field
    oldPermission.checkResetPermission();

    setPermissionFieldValue(obj, permission);
  }

  //
  // static private methods to do the dirty work
  //

  /**
   *  Retrieve the value of the field that holds the permission.
   *
   *  @param obj the object whose permission field value should be retrieved.
   *
   *  @return the permission for the given object, or null if the field has not
   *          been initialized, or the permission field does not exist (i.e.
   *          the object is an instance of a core Java class that ICP
   *          does not edit)
   */
  private static Permission getPermissionFieldValue(Object obj)
  {
    // use standard Java reflection
    Permission p = null;
    Field f = null;
    try {
      f = obj.getClass().getDeclaredField(AddedPermissionFieldName);
    }
    catch (NoSuchFieldException e)
    {
      // do nothing: f will remain null
    }
    if (f == null)
    {
      logger.fine("permission field is not present");
      return null;
    }
    try {
      f.setAccessible(true);
      p = (Permission) f.get(obj);
    }
    catch (Exception e)
    {
      logger.fine("exception while getting permission field: " + e);
      Message.fatal("get of permission field failed");
    }
    return p;
  }

  /**
   *  Initialize the value of the field that holds the permission to a
   *  private permission.
   *
   *  @param obj the object whose permission field value should be set.
   *
   */
  private static void initPermissionFieldValue(Object obj)
  {
    // InitialTask objects need a special permission
    Permission perm =  (obj instanceof InitialTask) ?
      InitialTaskPermission.get() :
      PrivatePermission.newInstance();

    setPermissionFieldValue(obj, perm);
  }

  /**
   *  Set the value of the permission field
   *
   *  @param obj the object whose permission field value should be set.
   *  @param permission the permission to place in the object.
   *
   */
  private static void setPermissionFieldValue(Object obj,
    Permission permission)
  {
    // use standard Java reflection
    Permission p = null;
    Field f = null;
    try {
      f = obj.getClass().getDeclaredField(AddedPermissionFieldName);
    }
    catch (NoSuchFieldException e)
    {
      logger.fine("exception while setting permission field: " + e);
      Message.fatal("permission field not found");
    }
    try {
      f.setAccessible(true);
      f.set(obj, permission);
    }
    catch (Exception e)
    {
      logger.fine("exception while setting permission field: " + e);
      Message.fatal("set of permission field failed");
    }
  }

}

