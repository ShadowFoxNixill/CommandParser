package net.nixill.commands.exceptions;

/**
 * An exception thrown whenever a deserializer catches another exception.
 * <p>
 * Deserializing methods should catch any known exceptions and throw them
 * wrapped in a DeserializationException so that the command parser itself can
 * catch that and show the user the error.
 * <p>
 * This exception can also be thrown during command registration if a default
 * value cannot be deserialized.
 * 
 * @author Nixill
 */
public class DeserializationException extends RuntimeException {
  private static final long serialVersionUID = -5851824025562670855L;
  
  /**
   * Constructs a DeserializationException with no message or cause.
   */
  public DeserializationException() {
    super();
  }
  
  /**
   * Constructs a DeserializationException with the given message.
   * 
   * @param message
   *          The message to show.
   */
  public DeserializationException(String message) {
    super(message);
  }
  
  /**
   * Constructs a DeserializationException with the given cause.
   * 
   * @param cause
   *          The error that was caught to throw the DeserializationException.
   */
  public DeserializationException(Throwable cause) {
    super(cause);
  }
}
