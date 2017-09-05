package net.nixill.commands.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.vdurmont.emoji.Emoji;

import net.nixill.commands.enums.MentionSetting;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;

/**
 * Marks the annotated method as a handler for a bot command.
 * <p>
 * A bot command method should:
 * <ul>
 * <li>Be public.</li>
 * <li>Return one of the following types:
 * <ul>
 * <li><code>void</code></li>
 * <li><code>String</code></li>
 * <li>{@link EmbedObject}</li>
 * <li>{@link IEmoji}</li>
 * <li>{@link Emoji}</li>
 * <li>Any other class for which a method annotated with @{@link Serializer}
 * exists.</li>
 * </ul>
 * <li>Take an {@link IMessage} as its first parameter.</li>
 * <li>Take an {@link IChannel} as its second parameter if it returns void.</li>
 * <li>Take further parameters representing the parameters the user passes to
 * the command. They may be of any type for which a method annotated
 * with @{@link Deserializer} exists, and may be annotated with @{@link Combine}
 * or @{@link OptParam}.</li>
 * </ul>
 * <p>
 * When the command parser is set up, it will parse the arguments of commands,
 * automatically throw errors if commands aren’t used correctly, and run the
 * method when the user uses the command correctly.
 * 
 * @author Nixill
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface BotCommand {
  /**
   * An enum to restrict the sources of a command.
   * 
   * @author Nixill
   */
  public enum CommandSource {
    /** The command will only work if used in a guild (server). */
    GUILD,
    /** The command will only work if used in a DM. */
    DM,
    /** The command will work in both DMs and servers. */
    BOTH;
  }
  
  /**
   * An enum to automatically pick a default reply target.
   * 
   * @author Nixill
   */
  public enum ReplyTarget {
    /** The default reply target will be the channel the command was sent in. */
    SOURCE,
    /** The main (#general) channel of the server. */
    GENERAL,
    /** The DM channel between the bot and the user that ran the command. */
    DM,
    /** The default reply target will be the channel specified in replyOther. */
    OTHER,
    /** There will be no default reply target. */
    NONE,
    /**
     * The default reply target will be the channel the command was sent in
     * (however, the default help will say that the bot will respond as a
     * reaction to the original message).
     */
    REACTION;
  }
  
  /**
   * The name of the command, as must be executed after the prefix and preceding
   * the parameters. For example, in the command
   * <code>@YourBot !helpwith help</code>, the command's name is
   * <code>helpwith</code>.
   * <p>
   * Command names should only include alphanumeric characters, hyphens, or
   * underscores. Other command names will fail.
   * <p>
   * If multiple commands attempt to use the same name, the latter command will
   * not register. If the bot uses the command parser's default help system,
   * <code>help</code> and <code>helpwith</code> are already taken, and cannot
   * be assigned to or overwritten by other commands.
   * <p>
   * Separate lists are kept for server- and DM-commands. If <code>listen</code>
   * is set to only <code>GUILD</code> or <code>DM</code>, only that list will
   * be checked for the command name's existence, and the name will only be
   * registered in that list. Otherwise, both lists will be checked and
   * registered (and having the name already exist in either list will cause the
   * command to fail in both).
   * <p>
   * This is a required value.
   * 
   * @see #names()
   * @return The command's name.
   */
  String name();
  
  /**
   * Additional names of the command, space-separated. A command can use any
   * name provided here, or its original name provided under <code>name</code>.
   * <p>
   * Command names should only include alphanumeric characters, hyphens, or
   * underscores. Other command names will fail.
   * <p>
   * If multiple commands attempt to use the same name (even as an alias), the
   * latter command will not register. If the bot uses the command parser's
   * default help system, <code>help</code> and <code>helpwith</code> are
   * already taken, and cannot be assigned to or overwritten by other commands.
   * <p>
   * Separate lists are kept for server- and DM-commands. If <code>listen</code>
   * is set to only <code>SERVER</code> or <code>DM</code>, only that list will
   * be checked for the command name's existence, and the name will only be
   * registered in that list. Otherwise, both lists will be checked and
   * registered (and having the name already exist in either list will cause the
   * command to fail in both).
   * <p>
   * This is an optional value, defaulting to an empty string (no aliases).
   * 
   * @return The command's aliases, space-separated.
   */
  String names() default "";
  
  /**
   * This defines where the command parser listens for commands.
   * <p>
   * This is an optional value, defaulting to <code>BOTH</code>.
   * 
   * @see CommandSource
   * @return The selected command source.
   */
  CommandSource listen() default CommandSource.BOTH;
  
  /**
   * Controls where the bot's default reply target will be. For command methods
   * that return a value, this value will be sent to that target automatically
   * (unless the value is an emoji, in which case it will be added as a reaction
   * to the command message). This target will also be shown in the default help
   * system, if used.
   * <p>
   * This is an optional value, defaulting to <code>SOURCE</code>.
   * 
   * @see ReplyTarget
   * @see #replyOther()
   * @return The default reply target.
   */
  ReplyTarget reply() default ReplyTarget.SOURCE;
  
  /**
   * Controls where the bot's default reply target will be, if set to a specific
   * channel (<code>ReplyTarget.OTHER</code>). Should be set to the ID of the
   * reply target.
   * <p>
   * This is an optional value, defaulting to 0.
   * 
   * @return
   */
  long replyOther() default 0L;
  
  /**
   * Controls whether a mention is needed to run the command.
   * <p>
   * This is an optional value, defaulting to <code>DEFAULT</code> (which is the
   * mention setting of the command parser itself).
   * 
   * @see net.nixill.commands.enums.MentionSetting
   * @return The selected mention setting.
   */
  MentionSetting mentions() default MentionSetting.DEFAULT;
  
  /**
   * The default usage string to be displayed to a user that doesn't use the
   * command properly.
   * <p>
   * This is a required value.
   * 
   * @return
   */
  String usage();
  
  /**
   * A string describing the command. Describe what it does and what its
   * parameters mean here.
   * <p>
   * This is an optional value, defaulting to an empty string.
   * 
   * @return The provided description.
   */
  String description() default "";
  
  /**
   * The permission the user is required to have for the command to run. Can be
   * used to make admin commands.
   * <p>
   * This is an optional value, defaulting to <code>SEND_MESSAGES</code> (which
   * equates to no restriction as someone who cannot send messages would be
   * unable to trigger a command in the first place).
   * 
   * @return The selected required permission.
   */
  Permissions requiredPerm() default Permissions.SEND_MESSAGES;
}
