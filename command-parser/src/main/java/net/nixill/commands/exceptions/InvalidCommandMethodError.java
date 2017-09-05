package net.nixill.commands.exceptions;

import net.nixill.commands.annotations.Deserializer;
import net.nixill.commands.annotations.Serializer;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Thrown when a plugin attempts to register an invalid command. This error
 * generally shouldn't be thrown by bot code.
 * <p>
 * This error can be thrown if:
 * <ul>
 * <li>The method does not return void or a serializable type (see
 * {@link Serializer}).</li>
 * <li>The method does not take only deserializable parameters (see
 * {@link Deserializer}).</li>
 * <li>The method has an invalid name or aliases.</li>
 * <li>The method does not have {@link IMessage} as its first parameter.</li>
 * <li>The method returns void, and does not have {@link IChannel} as its second
 * parameter.</li>
 * </ul>
 * <p>
 * This error will not be thrown for non-public methods. Those methods will
 * simply be skipped.
 * 
 * @author Nixill
 */
public class InvalidCommandMethodError extends Error {
  private static final long serialVersionUID = -3744014285022331698L;
  
  /**
   * Constructs an InvalidCommandMethodError.
   * 
   * @param message
   *          The message to add.
   */
  public InvalidCommandMethodError(String message) {
    super(message);
  }
}
