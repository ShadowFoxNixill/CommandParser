package net.nixill.commands.exceptions;

import java.util.ArrayList;

/**
 * Thrown when a plugin attempts to register an invalid deserialization method.
 * This error generally shouldn't be thrown by bot code.
 * <p>
 * This error can be thrown if:
 * <ul>
 * <li>The method does not take an {@link ArrayList}<code>&lt;String&gt;</code>
 * as its first argument, and an <code>int</code> as its second argument.</li>
 * <li>The method returns void.</li>
 * </ul>
 * <p>
 * This error will not be thrown for non-public methods. Those methods will
 * simply be skipped.
 * 
 * @author Nixill
 */
public class InvalidDeserializationMethodError extends Error {
  private static final long serialVersionUID = -6952068403952533212L;
  
  /**
   * Constructs the InvalidDeserializationMethodError.
   * 
   * @param message The message to use.
   */
  public InvalidDeserializationMethodError(String message) {
    super(message);
  }
}
