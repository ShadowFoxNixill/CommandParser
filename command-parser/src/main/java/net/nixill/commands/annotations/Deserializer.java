package net.nixill.commands.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;

/**
 * Marks the annotated method as a deserializer.
 * <p>
 * A deserializer should:
 * <ul>
 * <li>Be a public method.</li>
 * <li>Return an object type that you wish to use as a parameter in command
 * methods.</li>
 * <li>Take an {@link ArrayList}<code>&lt;String&gt;</code> and <code>int</code>
 * as its parameters (in that order).</li>
 * <li>Remove strings from the ArrayList to represent the single-word pieces of
 * the parameter.</li>
 * <li>Respect the int as how many strings to take, if possible.</li>
 * </ul>
 * 
 * @author Nixill
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Deserializer {}
