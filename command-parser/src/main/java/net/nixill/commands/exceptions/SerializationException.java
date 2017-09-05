package net.nixill.commands.exceptions;

/**
 * An exception thrown whenever a serializer catches another exception.
 * <p>
 * Serializing methods should catch any known exceptions and throw them
 * wrapped in a SerializationException so that the command parser itself can
 * catch that and show the user the error.
 * 
 * @author Nixill
 */
public class SerializationException extends RuntimeException {
  private static final long serialVersionUID = -5851824025562670855L;
  
  /**
   * Constructs a SerializationException with no message or cause.
   */
  public SerializationException() {
    super();
  }
  
  /**
   * Constructs a SerializationException with the given message.
   * 
   * @param message
   *          The message to show.
   */
  public SerializationException(String message) {
    super(message);
  }
  
  /**
   * Constructs a SerializationException with the given cause.
   * 
   * @param cause
   *          The error that was caught to throw the SerializationException.
   */
  public SerializationException(Throwable cause) {
    super(cause);
  }
}
