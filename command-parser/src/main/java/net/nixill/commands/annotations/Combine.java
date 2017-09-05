package net.nixill.commands.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Combines a specific number of single-word parameters into a single parameter.
 * By default it is only suppored by <code>String</code> parameters.
 * 
 * @author Nixill
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface Combine {
  /**
   * The number of single-word parameters to combine. The value is optional and
   * defaults to 2,147,483,647.
   * 
   * @return The supplied number.
   */
  int value() default Integer.MAX_VALUE;
}
