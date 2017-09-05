package net.nixill.commands.exceptions;

/**
 * Thrown when a plugin attempts to register a command with a name used by a
 * different command.
 * <p>
 * If you get this when attempting to register a command with the names "help"
 * or "helpwith", make sure you call a CommandReader constructor or setup method
 * with the boolean (<code>useDefHelp</code>) set to false.
 * 
 * @author Nixill
 */
public class NameAlreadyTakenError extends RuntimeException {
  private static final long serialVersionUID = -2179362145264461956L;
  
  /**
   * Constructs the NameAlreadyTakenError.
   * 
   * @param message The message to use.
   */
  public NameAlreadyTakenError(String message) {
    super(message);
  }
}
