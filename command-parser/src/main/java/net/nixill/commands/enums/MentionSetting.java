package net.nixill.commands.enums;

/**
 * Whether or not a command needs the bot to be @mentioned to run.
 * 
 * @author Nixill
 */
public enum MentionSetting {
  /**
   * Inherit the CommandReader's setting. If the CommandReader has this setting,
   * it is treated the same as <code>NO</code>.
   */
  DEFAULT,
  /**
   * The command requires a mention before the prefix, for example
   * <code>@YourBotName !help</code>.
   */
  PREFIX,
  /** The command does not require a mention to run. */
  NO;
}
