package net.nixill.commands.exceptions;

import com.vdurmont.emoji.Emoji;

import sx.blah.discord.api.internal.json.objects.EmbedObject;

/**
 * Thrown when a plugin attempts to register an invalid serialization method.
 * This error generally shouldn't be thrown by bot code.
 * <p>
 * This error can be thrown if:
 * <ul>
 * <li>The method does not return a <code>String</code>, {@link EmbedObject},
 * {@link Emoji}, or {@link IEmoji}.</li>
 * <li>The method doesn't take exactly one parameter (of any type).</li>
 * </ul>
 * <p>
 * This error will not be thrown for non-public methods. Those methods will
 * simply be skipped.
 * 
 * @author Nixill
 */
public class InvalidSerializationMethodError extends Error {
  private static final long serialVersionUID = -6952068403952533212L;
  
  /**
   * Constructs the InvalidSerializationMethodError.
   * 
   * @param message
   *          The message to use.
   */
  public InvalidSerializationMethodError(String message) {
    super(message);
  }
}
