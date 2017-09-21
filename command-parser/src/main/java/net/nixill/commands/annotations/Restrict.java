package net.nixill.commands.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Allows you to define a restriction on a single parameter without creating a
 * new class for the restriction. Exmaple restrictions include:
 * <ul>
 * <li>Even integers only: <code>@Restrict("even") int evenInteger</code></li>
 * <li>Odd multiples of five:
 * <code>@Restrict("odd & %5") int oddFive</code></li>
 * <li>North American phone numbers:
 * <code>@Restrict("\\d\\d\\d-\\d\\d\\d-\\d\\d\\d\\d") String phoneNumber"</code></li>
 * </ul>
 * <p>
 * The interpretation of a restriction is up to the deserializer method. To
 * handle a restriction, add a Restrict as a parameter after the ArrayList and
 * int, then get the <code>value()</code> of the Restrict to see what it is.
 * Note that if no restriction is provided, null will be passed to the method.
 * 
 * @author Nixill
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface Restrict {
  String value() default "";
  String error() default "";
}
