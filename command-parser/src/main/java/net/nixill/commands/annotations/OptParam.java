package net.nixill.commands.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a parameter of a command method as optional, and supplies a default
 * value.
 * <p>
 * It's worth noting that the deserializer will be run on the default value upon
 * the command's registration. However, the deserialized value won't be kept.
 * The command will fail to register if the value cannot be deserialized.
 * 
 * @author Nixill
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface OptParam {
  /**
   * The default value for the parameter. Write it as a string.
   * 
   * @return
   */
  String value();
}
