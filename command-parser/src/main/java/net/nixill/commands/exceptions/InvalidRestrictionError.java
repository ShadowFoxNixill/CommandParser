package net.nixill.commands.exceptions;

public class InvalidRestrictionError extends Error {
  private static final long serialVersionUID = -3283423595055955230L;
  public InvalidRestrictionError(String message) {
    super(message);
  }
}
