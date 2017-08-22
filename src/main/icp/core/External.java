// $Id$
package icp.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used on classes that should not be edited (e.g., classes that contain TestNG tests).
 * Annotated classes are still loaded my the {@code ICPLoader}, as opposed to {@code javassist} and
 * core classes, which are delegated to the default class loader.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface External {
}
