package net.nixill.commands.objects;

import java.util.ArrayList;

import net.nixill.commands.annotations.BotCommand.CommandSource;
import net.nixill.commands.annotations.BotCommand.ReplyTarget;
import net.nixill.commands.enums.MentionSetting;
import sx.blah.discord.api.internal.json.objects.EmbedObject.EmbedFieldObject;
import sx.blah.discord.handle.obj.Permissions;

/**
 * Represents a single command in the default help system.
 * 
 * @author Nixill
 */
class CommandHelp {
  private String            name;
  private ArrayList<String> aliases;
  private CommandSource     source;
  private String            usage;
  private String            description;
  private ReplyTarget       willReplyIn;
  private String            prefix;
  private MentionSetting    mentions;
  private Permissions       perm;
  private EmbedFieldObject  embed = null;
  
  /**
   * Creates the default command-help object.
   * 
   * @param name
   *          The name of the command.
   * @param aliases
   *          The aliases of the command.
   * @param source
   *          Where the command is listened for.
   * @param usage
   *          The usage of the command.
   * @param description
   *          The description of the command.
   * @param willReplyIn
   *          Where the bot will reply to the command.
   * @param prefix
   *          The required prefix for the command.
   * @param mentions
   *          Whether or not the command requires mentions.
   * @param perm
   *          What permission the command uses.
   */
  CommandHelp(String name, ArrayList<String> aliases, CommandSource source, String usage, String description,
      ReplyTarget willReplyIn, String prefix, MentionSetting mentions, Permissions perm) {
    this.name = name;
    this.aliases = aliases;
    this.source = source;
    this.usage = usage;
    this.description = description;
    this.willReplyIn = willReplyIn;
    this.mentions = mentions;
    this.prefix = prefix;
    this.perm = perm;
    
    updateEmbed();
  }
  
  /**
   * Returns the {@link EmbedFieldObject} containing help with the command.
   * 
   * @return The EmbedFieldObject.
   */
  EmbedFieldObject getEmbed() {
    return embed;
  }
  
  /**
   * Returns the <i>length</i> of the {@link EmbedFieldObject} containing help
   * with the command.
   * 
   * @return The length.
   */
  int getCharCount() {
    return embed.name.length() + embed.value.length();
  }
  
  /**
   * Updates the EmbedFieldObject with values.
   */
  private void updateEmbed() {
    String emName = "";
    String emValue = "";
    
    if (mentions == MentionSetting.PREFIX) {
      emName = CommandReader.getClient().getOurUser().mention() + " ";
    }
    
    emName += "**" + prefix;
    emName += name + "**";
    
    emValue += "**__Usage:__** " + usage;
    emValue += "\n\n";
    if (!description.isEmpty()) {
      emValue += description;
      emValue += "\n\n";
    }
    
    emValue += "**__Usable in:__** ";
    switch (source) {
      case BOTH:
        emValue += "Server and DM";
        break;
      case DM:
        emValue += "DM only";
        break;
      case GUILD:
        emValue += "Server only";
        break;
    }
    emValue += "\n\n";
    
    if (aliases.size() >= 2) {
      emValue += "**__Aliases:__** ";
      for (int i = 0; i < aliases.size(); i++) {
        if (i != 0) emValue += ", ";
        emValue += aliases.get(i);
      }
      emValue += "\n\n";
    }
    
    if (willReplyIn != ReplyTarget.NONE) {
      emValue += "**Bot will respond in:** ";
      switch (willReplyIn) {
        case DM:
          emValue += "Direct message to caller.";
          break;
        case SOURCE:
          emValue += "Channel command was called in.";
          break;
        case GENERAL:
          emValue += "General channel of original server.";
          break;
        case REACTION:
          emValue += "A reaction to the original message.";
          break;
        case OTHER:
          emValue += "A specific channel of a specific server.";
          break;
        default:
          // Do nothing
      }
      emValue += "\n\n";
    }
    
    if (perm != Permissions.SEND_MESSAGES) {
      emValue += "**Requires permission:** ";
      emValue += perm.toString();
      emValue += "\n\n";
    }
    
    emValue += "---";
    
    embed = new EmbedFieldObject(emName, emValue, false);
  }
}
