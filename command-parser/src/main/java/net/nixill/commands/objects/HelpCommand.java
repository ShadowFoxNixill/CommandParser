package net.nixill.commands.objects;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NavigableMap;
import java.util.TreeMap;

import net.nixill.commands.annotations.BotCommand;
import net.nixill.commands.annotations.BotCommand.ReplyTarget;
import net.nixill.commands.annotations.OptParam;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.api.internal.json.objects.EmbedObject.EmbedFieldObject;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * A class holding information for the command reader's default Help system,
 * automatically populating it with information on the given commands if it's
 * used.
 * 
 * @author Nixill
 */
class HelpCommand {
  /** The CommandReader using the help system. */
  private CommandReader                reader;
  /** The pages of the command help. */
  private TreeMap<Integer, String>     commandPages     = new TreeMap<>();
  /** The embeds for each command (by their main name). */
  private TreeMap<String, CommandHelp> mainEmbeds       = new TreeMap<>();
  /** The embeds for each command (by all aliases). */
  private HashMap<String, CommandHelp> aliasEmbeds      = new HashMap<>();
  
  /** The name at the top of the embed. */
  private String                       embedName;
  /** The description at the top of the embed. */
  private String                       embedDescription = "";
  /** The footer at the bottom of the embed. */
  private String                       embedFooter      = "Page XXX of XXX";
  
  /**
   * Initializes a HelpCommand.
   * 
   * @param r
   *          The CommandReader that's launching the HelpCommand.
   */
  HelpCommand(CommandReader r) {
    reader = r;
    embedName = CommandReader.getClient().getOurUser().getName() + " help";
  }
  
  /**
   * Sets the description of the embed returned by the help.
   * 
   * @param description
   *          The description to be set.
   */
  void setDescription(String description) {
    embedDescription = description;
    repaginate();
  }
  
  /**
   * Fired by the command reader when the command !help is used by a user. Sends
   * the user a DM displaying a list of commands, including "!help" itself.
   * 
   * @param msg
   *          The incoming message.
   * @param page
   *          Which page to view.
   * @return An {@link EmbedObject} showing the specified page for help.
   */
  @BotCommand(name = "help", usage = "help [page]", description = "Displays a list of commands.", reply = ReplyTarget.DM)
  public EmbedObject helpCommand(IMessage msg, @OptParam("1") int page) {
    EmbedBuilder em = new EmbedBuilder();
    em.withTitle(embedName).withDesc(embedDescription).withFooterText("Page " + page + " of " + commandPages.size());
    if (commandPages.containsKey(page)) {
      String firstKey = commandPages.get(page);
      String lastKey = commandPages.get(page + 1);
      if (lastKey == null)
        lastKey = mainEmbeds.lastKey();
      else
        lastKey = mainEmbeds.lowerKey(lastKey);
      
      NavigableMap<String, CommandHelp> subMap = mainEmbeds.subMap(firstKey, true, lastKey, true);
      
      String key = firstKey;
      while (key != null) {
        EmbedFieldObject embed = mainEmbeds.get(key).getEmbed();
        em.appendField(embed.name, embed.value, embed.inline);
        key = subMap.higherKey(key);
      }
    } else if (page <= 0) {
      em.appendField("Not far enough.",
          "Negative and zero pages do not exist. Please try again with a positive number.", false);
    }
    return em.build();
  }
  
  /**
   * Fired by the command reader when the command !helpwith is used by a user.
   * Sends the user a DM displaying help on the specified command (or commands,
   * if separate commands by that name exist for DM and Guild).
   * 
   * @param msg
   *          The incoming message.
   * @param whatCommand
   *          Which command to view.
   * @return An {@link EmbedObject} showing the specified page for help.
   */
  @BotCommand(name = "helpwith", usage = "!helpwith <command>", description = "Displays help on a specific command.", reply = ReplyTarget.DM)
  public EmbedObject helpWithCommand(IMessage msg, String whatCommand) {
    EmbedBuilder em = new EmbedBuilder();
    whatCommand = whatCommand.toLowerCase();
    em.withTitle(CommandReader.getClient().getOurUser().getDisplayName(msg.getGuild()) + ": " + whatCommand + " help");
    // Insert that command's embed fields
    return em.build();
  }
  
  /**
   * Splits the list of commands into pages based on the lengths of the command
   * embeds.
   */
  void repaginate() {
    commandPages.clear();
    
    int builtInChars = embedName.length() + embedDescription.length() + embedFooter.length();
    int remainingChars = -1;
    
    int page = 0;
    String key = mainEmbeds.firstKey();
    CommandHelp cHelp = mainEmbeds.get(key);
    
    while (true) {
      remainingChars -= cHelp.getCharCount();
      if (remainingChars < 0) {
        page += 1;
        commandPages.put(page, key);
        remainingChars = 750 - builtInChars;
        remainingChars -= cHelp.getCharCount();
      }
      
      key = mainEmbeds.higherKey(key);
      if (key == null) return;
    }
  }
  
  /**
   * Adds a command method to the help system. Does not automatically
   * repaginate, so as to save time adding multiple commands.
   * 
   * @param meth
   *          The method to add.
   * @param acceptedAliases
   *          The aliases for that command that weren't already taken by other
   *          commands.
   */
  void addCommand(Method meth, ArrayList<String> acceptedAliases) {
    BotCommand cmd = meth.getAnnotation(BotCommand.class);
    
    // Create new help object
    CommandHelp cHelp = new CommandHelp(cmd.name(), acceptedAliases, cmd.listen(), cmd.usage(), cmd.description(),
        cmd.reply(), reader.getPrefix(), reader.getMentionSetting(cmd.mentions()), cmd.requiredPerm());
    
    // Add to main list
    switch (cmd.listen()) {
      case GUILD:
        mainEmbeds.put(cmd.name() + " server", cHelp);
        break;
      case DM:
        mainEmbeds.put(cmd.name() + " dm", cHelp);
        break;
      case BOTH:
        mainEmbeds.put(cmd.name(), cHelp);
    }
    
    // Add to sub-lists
    for (String name : acceptedAliases) {
      switch (cmd.listen()) {
        case GUILD:
          aliasEmbeds.put(name + " server", cHelp);
          break;
        case DM:
          aliasEmbeds.put(name + " dm", cHelp);
          break;
        case BOTH:
          aliasEmbeds.put(name, cHelp);
      }
    }
  }
}
