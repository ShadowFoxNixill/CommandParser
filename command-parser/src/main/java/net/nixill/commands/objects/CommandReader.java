package net.nixill.commands.objects;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vdurmont.emoji.Emoji;

import net.nixill.commands.annotations.BotCommand;
import net.nixill.commands.annotations.Combine;
import net.nixill.commands.annotations.Deserializer;
import net.nixill.commands.annotations.OptParam;
import net.nixill.commands.annotations.Restrict;
import net.nixill.commands.annotations.Serializer;
import net.nixill.commands.enums.MentionSetting;
import net.nixill.commands.exceptions.DeserializationException;
import net.nixill.commands.exceptions.InvalidCommandMethodError;
import net.nixill.commands.exceptions.InvalidDeserializationMethodError;
import net.nixill.commands.exceptions.InvalidSerializationMethodError;
import net.nixill.commands.exceptions.NameAlreadyTakenError;
import net.nixill.commands.exceptions.SerializationException;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IMessage;

/**
 * The main class for command interpretation. A CommandReader provides all the
 * functionality for listening for commands, checking the syntax and structure,
 * running deserializers to convert user input into primitive or object types,
 * calling command methods when commands are run, and even sending returned
 * results or errors in input back to the user.
 * <p>
 * It also has the option to create its own help command system, but bots that
 * implement CommandReader may also make their own (they just need to call the
 * CommandReader with the boolean <code>useDefHelp</code> false).
 * <p>
 * To set up the bot through the command reader, use the {@link #setup} method.
 * The method will create and login the bot, register the CommandReader as a
 * message listener, and register the bot class' deserializers, serializers, and
 * commands to the CommandReader. It will then return the CommandReader for
 * further registration. The client object can be obtained through the
 * {@link #getClient()} method.
 * <p>
 * Otherwise, if you instantiate the IDiscordClient yourself, you can supply it
 * to the CommandReader using the {@link #Constructor}.
 * 
 * @author Nixill
 */
public class CommandReader {
  /** The discord client */
  private static IDiscordClient     client;
  
  /** The registered deserializers. */
  private HashMap<Class<?>, Method> deserializers;
  /** The objects for the registered deserializers. */
  private HashMap<Class<?>, Object> deserializerObjects;
  
  /** The registered serializers. */
  private HashMap<Class<?>, Method> serializers;
  /** The objects for the registered serializers. */
  private HashMap<Class<?>, Object> serializerObjects;
  
  /** The commands registered to guilds. */
  private HashMap<String, Method>   serverCommands;
  /** The objects for the registered commands. */
  private HashMap<String, Object>   serverCommandObjects;
  
  /** The commands registered to direct messages. */
  private HashMap<String, Method>   dmCommands;
  /** The objects for the registered DM commands. */
  private HashMap<String, Object>   dmCommandObjects;
  
  /** The objects to register upon ready (list only exists before ready). */
  private ArrayList<Object>         delayedRegistration;
  
  /** The default Help system object, if it's used. */
  private HelpCommand               helpCommand;
  
  /** The prefix for all commands. */
  private String                    prefix;
  
  /** The default mention-requirement setting for all commands. */
  private MentionSetting            requireMention;
  
  /** Whether the default help system is enabled for this reader. */
  private boolean                   useDefaultHelp;
  
  {
    serverCommands = new HashMap<>();
    dmCommands = new HashMap<>();
    deserializers = new HashMap<>();
    serializers = new HashMap<>();
    
    serverCommandObjects = new HashMap<>();
    dmCommandObjects = new HashMap<>();
    deserializerObjects = new HashMap<>();
    serializerObjects = new HashMap<>();
    
    prefix = "!";
    requireMention = MentionSetting.NO;
  }
  
  /**
   * Sets up the bot. In particular, does so by performing the following steps:
   * <ol>
   * <li>Creates an IDiscordClient with the recommended shard count and the
   * provided token, and logs it in.</li>
   * <li>Creates a new CommandReader with the provided setting for using (or
   * ignoring) the default help system.</li>
   * <li>Registers <i>static</i> deserializers, serializers, and commands from
   * the provided class in the CommandReader.</li>
   * <li>Registers the CommandReader as a message listener for the discord
   * bot.</li>
   * </ol>
   * 
   * @param botClass
   *          The class with the serializers, deserializers, and commands.
   * @param token
   *          The token to log in.
   * @param useDefHelp
   *          Whether or not to use the built-in help system.
   * @return The CommandReader itself, for additional registrations.
   */
  public static CommandReader setup(Class<?> botClass, String token, boolean useDefHelp) {
    client = new ClientBuilder().withRecommendedShardCount().withToken(token).login();
    CommandReader reader = new CommandReader(useDefHelp).register(botClass);
    client.getDispatcher().registerListener(reader);
    return reader;
  }
  
  /**
   * Sets up the bot. In particular, does so by performing the following steps:
   * <ol>
   * <li>Creates an IDiscordClient with the recommended shard count and the
   * provided token, and logs it in.</li>
   * <li>Creates a new CommandReader with the provided setting for using (or
   * ignoring) the default help system.</li>
   * <li>Registers deserializers, serializers, and commands from the provided
   * class in the CommandReader.</li>
   * <li>Registers the CommandReader as a message listener for the discord
   * bot.</li>
   * </ol>
   * 
   * @param bot
   *          The instance of the class with the serializers, deserializers, and
   *          commands. Methods will be run from the supplied instance.
   * @param token
   *          The token to log in.
   * @param useDefHelp
   *          Whether or not to use the built-in help system.
   * @return The CommandReader itself, for additional registrations.
   */
  public static CommandReader setup(Object bot, String token, boolean useDefHelp) {
    client = new ClientBuilder().withRecommendedShardCount().withToken(token).login();
    CommandReader reader = new CommandReader(useDefHelp).register(bot);
    client.getDispatcher().registerListener(reader);
    return reader;
  }
  
  /**
   * Creates a new CommandReader with an existing client.
   * 
   * @param useDefHelp
   *          Whether or not to use the built-in help system.
   */
  private CommandReader(boolean useDefHelp) {
    this(client, useDefHelp);
  }
  
  /**
   * Creates a new CommandReader. This should be used for manual setup - for
   * automatic/default setup, use {@link #setup}.
   * 
   * @param cli
   * @param useDefHelp
   */
  public CommandReader(IDiscordClient cli, boolean useDefHelp) {
    client = cli;
    useDefaultHelp = useDefHelp;
    if (useDefaultHelp && cli.isReady()) {
      helpCommand = new HelpCommand(this);
      register(helpCommand);
    }
    register(DefaultMethods.class);
    if (!cli.isReady()) delayedRegistration = new ArrayList<>();
  }
  
  /**
   * Fired by Discord4J when client setup is complete. Should not be called by
   * bot code.
   * 
   * @param event
   *          The event.
   */
  @EventSubscriber
  public void onReady(ReadyEvent event) {
    if (useDefaultHelp) {
      helpCommand = new HelpCommand(this);
      register(helpCommand);
    }
    for (Object obj : delayedRegistration) {
      if (obj instanceof Class)
        register((Class<?>) obj);
      else
        register(obj);
    }
    delayedRegistration = null;
  }
  
  /**
   * Register the deserializers, serializers, and bot commands in a class.
   * <p>
   * Using this method registers both static and instance methods. Instance
   * methods will be fired from the supplied instance. To register static
   * methods only, use {@link #register(Class)}.
   * 
   * @param object
   *          The object from which methods should be registered.
   * @return The CommandReader itself, for chaining.
   */
  public CommandReader register(Object object) {
    return register(object.getClass(), object);
  }
  
  /**
   * Register the <i>static</i> deserializers, serializers, and bot commands in
   * a class.
   * <p>
   * Using this method registered only static methods. To register instance
   * methods too, use {@link #register(Object)}.
   * <p>
   * You should not use both <code>register(Class)</code> and
   * <code>register(Object)</code> on the same class, as it will cause a
   * conflict in command names.
   * 
   * @param cls
   *          The class from which static methods should be registered.
   * @return The CommandReader itself, for chaining.
   */
  public CommandReader register(Class<?> cls) {
    return register(cls, null);
  }
  
  /**
   * Performs the actual registration of commands.
   * 
   * @param cls
   *          A class to register commands from.
   * @param object
   *          The object of that class from which to fire instance methods.
   *          <code>null</code> for static methods only.
   * @return The CommandReader itself, for chaining.
   */
  private CommandReader register(Class<?> cls, Object object) {
    if (!client.isReady() && delayedRegistration != null) {
      if (object == null)
        delayedRegistration.add(cls);
      else
        delayedRegistration.add(object);
      return this;
    }
    
    ArrayList<Method> meths = new ArrayList<>(Arrays.asList(cls.getDeclaredMethods()));
    
    for (Method meth : meths) {
      Deserializer des = meth.getAnnotation(Deserializer.class);
      Serializer ser = meth.getAnnotation(Serializer.class);
      int mods = meth.getModifiers();
      boolean isStatic = Modifier.isStatic(mods);
      boolean isPublic = Modifier.isPublic(mods);
      if (des != null && !(object == null && !isStatic) && isPublic) {
        addDeserializer(meth, object);
      }
      if (ser != null && !(object == null && !isStatic) && isPublic) {
        addSerializer(meth, object);
      }
    }
    
    for (Method meth : meths) {
      BotCommand botc = meth.getAnnotation(BotCommand.class);
      int mods = meth.getModifiers();
      boolean isStatic = Modifier.isStatic(mods);
      boolean isPublic = Modifier.isPublic(mods);
      if (botc != null && !(object == null && !isStatic) && isPublic) {
        addCommand(botc, meth, object);
      }
    }
    
    if (helpCommand != null) {
      helpCommand.repaginate();
    }
    
    return this;
  }
  
  /**
   * Add a deserializer method.
   * 
   * @param meth
   *          The method to add.
   * @param obj
   *          The object to fire it from.
   */
  private void addDeserializer(Method meth, Object obj) {
    Parameter[] params = meth.getParameters();
    int parCount = params.length;
    if (parCount < 2) throw new InvalidDeserializationMethodError(
        "Deserialization methods must have at least two parameters. The first two must be an ArrayList and an int.");
    if (!params[0].getType().equals(ArrayList.class)) throw new InvalidDeserializationMethodError(
        "Deserialization methods must have at least two parameters. The first two must be an ArrayList and an int.");
    if (!params[1].getType().equals(int.class)) throw new InvalidDeserializationMethodError(
        "Deserialization methods must have at least two parameters. The first two must be an ArrayList and an int.");
    for (int i = 2; i < meth.getParameterCount(); i++) {
      Class<?> cls = params[i].getType();
      if (cls != IMessage.class && cls != Restrict.class && cls != Boolean.class
          && cls == Boolean.TYPE) { throw new InvalidDeserializationMethodError(
              "Deserialization methods can only have an IMessage, a Restrict annotation, or a boolean as additional "
                  + "arguments."); }
    }
    Class<?> ret = meth.getReturnType();
    deserializers.put(ret, meth);
    if (Modifier.isStatic(meth.getModifiers()))
      deserializerObjects.put(ret, null);
    else
      deserializerObjects.put(ret, obj);
  }
  
  /**
   * Add a serializer method.
   * 
   * @param meth
   *          The method to add.
   * @param obj
   *          The object to fire it from.
   */
  private void addSerializer(Method meth, Object obj) {
    Class<?>[] parTypes = meth.getParameterTypes();
    int parCount = parTypes.length;
    if (parCount != 1 && parCount != 2) throw new InvalidSerializationMethodError(
        "Serialization methods must have exactly one or two parameters - the type you wish to return from command "
            + "methods, and optionally an IMessage.");
    Class<?> in = parTypes[0];
    Class<?> ret = meth.getReturnType();
    if (!(ret.isAssignableFrom(String.class) || ret.isAssignableFrom(EmbedObject.class)
        || ret.isAssignableFrom(Emoji.class)
        || ret.isAssignableFrom(IEmoji.class))) { throw new InvalidDeserializationMethodError(
            "Serialization methods must return either a String, EmbedObject, Emoji, IEmoji, or void."); }
    if (parCount == 2 && !parTypes[1].equals(IMessage.class)) throw new InvalidSerializationMethodError(
        "Serialization methods with two parameters must take the command return type and IMessage, in that order.");
    serializers.put(in, meth);
    if (Modifier.isStatic(meth.getModifiers()))
      serializerObjects.put(in, null);
    else
      serializerObjects.put(in, obj);
  }
  
  /**
   * Add a command method.
   * 
   * @param ann
   *          The method's annotation.
   * @param meth
   *          The method itself.
   * @param obj
   *          The object to fire it from.
   */
  private void addCommand(BotCommand ann, Method meth, Object obj) {
    // Ensure the first parameter is correct - an IMessage.
    ArrayList<Parameter> params = new ArrayList<>(Arrays.asList(meth.getParameters()));
    
    if (params.size() < 1) throw new InvalidCommandMethodError(
        "Command methods must have at least one parameter (sx.blah.discord.handle.obj.IMessage).");
    if (!params.get(0).getType().isAssignableFrom(IMessage.class)) throw new InvalidCommandMethodError(
        "The first parameter of command methods must be sx.blah.discord.handle.obj.IMessage.");
    
    // Ensure the return type is correct, and if void, that the second parameter
    // is an IChannel.
    Class<?> returnType = meth.getReturnType();
    if (returnType.equals(Void.TYPE)) {
      if (params.size() < 2) throw new InvalidCommandMethodError(
          "Void command methods must have at least two parameters (second must be sx.blah.discord.handle.obj.IChannel).");
      if (!params.get(1).getType().isAssignableFrom(IChannel.class)) throw new InvalidCommandMethodError(
          "The second parameter of void command methods must be sx.blah.discord.handle.obj.IChannel.");
      params.remove(0);
      params.remove(0);
    } else {
      params.remove(0);
    }
    
    // Ensure the remaining types are deserializable.
    for (Parameter par : params) {
      Class<?> cls = par.getType();
      if (cls.isArray()) {
        Class<?> clz = cls.getComponentType();
        if (!(deserializers.containsKey(clz) || clz.isEnum())) throw new InvalidCommandMethodError(
            "Type " + clz.getName() + " has no String converter (try registering deserializers first).");
      } else if (!(deserializers.containsKey(cls) || cls.isEnum())) throw new InvalidCommandMethodError(
          "Type " + cls.getName() + " has no String converter (try registering deserializers first).");
    }
    
    // Make sure the main name isn't already taken.
    assertNameAvailable(ann.name(), ann.listen());
    
    // Add to list(s). It's possible to have different methods handle DM vs
    // Guild commands.
    String[] names = (ann.name() + " " + ann.names()).split(" ");
    if (Modifier.isStatic(meth.getModifiers())) obj = null;
    ArrayList<String> allowedAliases = new ArrayList<>();
    for (String name : names) {
      if (addName(meth, obj, name, ann.listen())) {
        allowedAliases.add(name.toLowerCase());
      }
    }
    
    // If using the default help, register this command with the help.
    if (helpCommand != null) {
      helpCommand.addCommand(meth, allowedAliases);
    }
  }
  
  /**
   * Asserts that a command name is available and errors otherwise.
   * 
   * @param name
   *          The name to check for.
   * @param where
   *          Which list to look in.
   * @throws InvalidCommandMethodError
   *           If the name is invalid.
   * @throws NameAlreadyTakenError
   *           If the name is already taken.
   */
  private void assertNameAvailable(String name, BotCommand.CommandSource where) {
    name = name.toLowerCase();
    Matcher mtc = Pattern.compile("[a-z0-9\\-_]+").matcher(name);
    if (!mtc.matches()) throw new InvalidCommandMethodError("The command name " + name
        + " is not valid. Names can only contain numbers, letters, underscores, and hyphens.");
    if (where != BotCommand.CommandSource.DM) {
      if (serverCommands.containsKey(name))
        throw new NameAlreadyTakenError("The command name " + name + " is already taken by command "
            + serverCommands.get(name).getAnnotation(BotCommand.class).name().toLowerCase());
    }
    if (where != BotCommand.CommandSource.GUILD) {
      if (dmCommands.containsKey(name))
        throw new NameAlreadyTakenError("The command name " + name + " is already taken by command "
            + dmCommands.get(name).getAnnotation(BotCommand.class).name().toLowerCase());
    }
  }
  
  /**
   * Adds a command to the lists.
   * 
   * @param meth
   *          The method to add.
   * @param obj
   *          The object to fire the method from.
   * @param name
   *          The name to add it under.
   * @param where
   *          Which list to add it to.
   * @return Whether or not the command was actually added by that name.
   */
  private boolean addName(Method meth, Object obj, String name, BotCommand.CommandSource where) {
    name = name.toLowerCase();
    Matcher mtc = Pattern.compile("[a-z0-9\\-_]+").matcher(name);
    if (!mtc.matches()) return false;
    if (where != BotCommand.CommandSource.DM) {
      if (serverCommands.containsKey(name)) return false;
    }
    if (where != BotCommand.CommandSource.GUILD) {
      if (dmCommands.containsKey(name)) return false;
      dmCommands.put(name, meth);
      dmCommandObjects.put(name, obj);
    }
    if (where != BotCommand.CommandSource.DM) {
      serverCommands.put(name, meth);
      serverCommandObjects.put(name, obj);
    }
    return true;
  }
  
  /**
   * Fired by Discord whenever a message is received. Triggers commands when
   * they match. Shouldn't be fired by bot code.
   * 
   * @param event
   *          The event.
   */
  @EventSubscriber
  public void handle(MessageReceivedEvent event) {
    IMessage msg = event.getMessage();
    String messageTxt = msg.getContent().trim();
    
    // See if the message starts with a pre-mention
    boolean preMention = false;
    if (messageTxt.startsWith(client.getOurUser().mention(true))) {
      preMention = true;
      messageTxt = messageTxt.substring(client.getOurUser().mention(true).length()).trim();
    } else if (messageTxt.startsWith(client.getOurUser().mention(false))) {
      preMention = true;
      messageTxt = messageTxt.substring(client.getOurUser().mention(false).length()).trim();
    }
    
    // See if the command starts with the proper prefix; cancel the event if not
    if (!messageTxt.startsWith(prefix)) return;
    messageTxt = messageTxt.substring(prefix.length());
    
    // Split the command into its parameters, and make sure some *actually
    // exist* (at the very least, the name of the command)
    ArrayList<String> paramStr = new ArrayList<>(Arrays.asList(messageTxt.split(" ")));
    if (paramStr.isEmpty()) return;
    
    // Get objects for the comm
    Method meth = null;
    Object obj = null;
    BotCommand cmd = null;
    
    String cmdName = paramStr.get(0).toLowerCase();
    
    if (event.getChannel().isPrivate()) {
      meth = dmCommands.get(cmdName);
      obj = dmCommandObjects.get(cmdName);
    } else {
      meth = serverCommands.get(cmdName);
      obj = serverCommandObjects.get(cmdName);
    }
    
    paramStr.remove(0);
    
    // If the command doesn't exist at the given source, return.
    if (meth == null) return;
    
    // Get the command annotation
    cmd = meth.getAnnotation(BotCommand.class);
    
    // Check if a prefix mention is required, and if so, was one supplied?
    boolean isPreMentionRequired = (cmd.mentions() == MentionSetting.PREFIX)
        || (cmd.mentions() == MentionSetting.DEFAULT && requireMention == MentionSetting.PREFIX);
    if (isPreMentionRequired & !preMention) return;
    
    // We'll need this later. Like, almost a hundred lines from now.
    boolean isVoidMethod = false;
    
    ArrayList<Parameter> paramTypes = new ArrayList<>(Arrays.asList(meth.getParameters()));
    paramTypes.remove(0);
    Class<?> methType = meth.getReturnType();
    if (methType.equals(Void.TYPE)) {
      isVoidMethod = true;
      paramTypes.remove(0);
    }
    
    // Get the target channel to send messages to; we'll need it now to send an
    // error if the command fails
    IChannel replyTarget = null;
    BotCommand.ReplyTarget targ = cmd.reply();
    switch (targ) {
      case SOURCE:
      case REACTION:
      case NONE: // Use source channel in case of errors.
        replyTarget = event.getChannel();
        break;
      case GENERAL:
        replyTarget = event.getGuild().getGeneralChannel();
        if (replyTarget == null) replyTarget = event.getChannel();
        break;
      case DM:
        replyTarget = event.getAuthor().getOrCreatePMChannel();
        break;
      case OTHER:
        replyTarget = client.getChannelByID(cmd.replyOther());
        if (replyTarget == null) replyTarget = event.getChannel();
        break;
    }
    
    // Check if the user is permitted to perform this command
    if (!event.getChannel().getModifiedPermissions(event.getAuthor()).contains(cmd.requiredPerm())) {
      MessageSender.send(replyTarget,
          "You can't use this command because you don't have the " + cmd.requiredPerm().toString() + " permission.");
      return;
    }
    
    // Now make the list for parameters
    ArrayList<Object> params = new ArrayList<>(paramTypes.size() + 2);
    
    // Only check incoming parameters if there are parameters in the command
    // method.
    if (!paramTypes.isEmpty()) {
      // Now start parsing parameters.
      if (paramTypes.size() > 0) {
        int maxParam = paramTypes.size() - 1;
        for (int i = 0; i < maxParam + 1; i++) {
          Parameter param = paramTypes.get(Math.min(maxParam, i));
          OptParam opt = param.getAnnotation(OptParam.class);
          
          if (paramStr.isEmpty()) {
            if (opt != null) {
              paramStr = new ArrayList<>(Arrays.asList(opt.value().split(" ")));
            } else {
              String usage = cmd.usage();
              if (!usage.isEmpty())
                MessageSender.send(replyTarget, "Usage: " + usage);
              else
                MessageSender.send(replyTarget, "Not enough parameters (no usage string provided)");
              return;
            }
          }
          
          Combine comb = param.getAnnotation(Combine.class);
          int howMany = Integer.MAX_VALUE;
          if (comb != null) {
            howMany = comb.value();
          } else {
            if (!param.getType().isArray()) howMany = 1;
          }
          
          Restrict rest = param.getAnnotation(Restrict.class);
          
          try {
            Object parObj;
            parObj = deserialize(param.getType(), paramStr, howMany, msg, rest);
            params.add(parObj);
          } catch (DeserializationException ex) {
            MessageSender.send(replyTarget, "Parameter " + (i + 1) + " is invalid: "
                + ex.getMessage());
            return;
          }
        }
      }
    }
    
    // Add the message, and optionally the channel, to the start of the list.
    params.add(0, event.getMessage());
    if (isVoidMethod) params.add(1, replyTarget);
    
    // Change it to an array to be compatible with the method.invoke method.
    Object[] args = params.toArray();
    
    // And now run the method!
    Object retVal = null;
    try {
      retVal = meth.invoke(obj, args);
    } catch (IllegalAccessException | IllegalArgumentException e) {
      MessageSender.send(replyTarget,
          "An error occurred because Nix didn't learn how Java Reflection works. .w. Have some details:\n"
              + e.toString() + "\n" + e.getStackTrace()[0] + "\n" + e.getStackTrace()[1]);
    } catch (InvocationTargetException e) {
      Throwable ex = e.getCause();
      MessageSender.send(replyTarget,
          "An error occurred. Have some details:\n"
              + ex.toString() + "\n" + ex.getStackTrace()[0] + "\n" + ex.getStackTrace()[1] + "\n"
              + ex.getStackTrace()[2] + "\n" + ex.getStackTrace()[3] + "\n" + ex.getStackTrace()[4]);
    }
    
    // Lastly, let's do something with the result.
    // If it's null or no reply target was specified, we're done with our job,
    // even if the return type isn't void.
    if (retVal == null || targ == BotCommand.ReplyTarget.NONE) return;
    
    // Serialize the result first.
    Class<?> retType = retVal.getClass();
    if (!(retType.isAssignableFrom(String.class) || retType.isAssignableFrom(EmbedObject.class)
        || retType.isAssignableFrom(IEmoji.class) || retType.isAssignableFrom(Emoji.class))) {
      retVal = serialize(retVal, methType, msg);
    }
    
    // If it's a string or EmbedObject, we're sending it at the reply target.
    if (retVal instanceof String) {
      MessageSender.send(replyTarget, (String) retVal);
      return;
    }
    
    if (retVal instanceof EmbedObject) {
      MessageSender.send(replyTarget, (EmbedObject) retVal);
      return;
    }
    
    // If it's an emoji (custom or standard), we're reacting to the original
    // message with it.
    if (retVal instanceof Emoji) {
      MessageSender.react(event.getMessage(), (Emoji) retVal);
      return;
    }
    
    if (retVal instanceof IEmoji) {
      MessageSender.react(event.getMessage(), (IEmoji) retVal);
      return;
    }
  }
  
  /**
   * Converts a string input by the user to an object accepted by the bot
   * command.
   * 
   * @param cls
   *          The class to which the string should be converted.
   * @param values
   *          The strings (space separated) input by the user.
   * @param howMany
   *          Either the value of the <code>@</code>{@link Combine} annotation,
   *          or <code>Integer.MAX_VALUE</code> for arrays/varargs and 1 for
   *          anything else.
   * @param msg
   *          The message that triggered this command.
   * @param restriction
   *          The <code>@</code>{@link Restrict} tag passed to the parameter.
   * @return The object to be sent to the command.
   */
  @SuppressWarnings("unchecked")
  <E> E deserialize(Class<E> cls, ArrayList<String> values, int howMany, IMessage msg, Restrict restriction) {
    if (cls.isArray() && !deserializers.containsKey(cls)) {
      return (E) deserializeArray(cls.getComponentType(), values, howMany, msg, restriction);
    } else if (cls.isEnum() && !deserializers.containsKey(cls)) {
      String value = values.remove(0);
      E[] constants = cls.getEnumConstants();
      for (E constant : constants) {
        if (constant.toString().toLowerCase().equals(value.toLowerCase())) return constant;
      }
      throw new DeserializationException(value + " is an invalid choice.");
    } else {
      try {
        Method meth = deserializers.get(cls);
        Object obj = deserializerObjects.get(cls);
        Object[] inputs = new Object[meth.getParameterCount()];
        Class<?>[] inputTypes = meth.getParameterTypes();
        inputs[0] = values;
        inputs[1] = howMany;
        for (int i = 2; i < inputs.length; i++) {
          Class<?> clz = inputTypes[i];
          if (clz == IMessage.class) inputs[i] = msg;
          if (clz == Restrict.class) inputs[i] = restriction;
          // Forwards compatibility - the boolean is whether the message was
          // deleted (true) or created (false).
          if (clz == Boolean.TYPE || clz == Boolean.class) inputs[i] = Boolean.FALSE;
        }
        return (E) meth.invoke(obj, inputs);
      } catch (IllegalAccessException | IllegalArgumentException ex) {
        throw new DeserializationException("The deserialization method for type " + cls.getSimpleName() + " failed.");
      } catch (InvocationTargetException ex) {
        Throwable th = ex.getCause();
        if (th instanceof DeserializationException) throw (DeserializationException) th;
        if (th instanceof Error) throw (Error) th;
        else throw new DeserializationException(th);
      }
    }
  }
  
  /**
   * Converts strings input by the user into an array of objects using the
   * non-array's deserializer.
   * <p>
   * Primitive types cannot be deserialized using this method. However, they
   * have default array deserializers in {@link DefaultMethods}.
   * 
   * @param cls
   *          The component type of the array to which the string should be
   *          converted.
   * @param values
   *          The strings (space separated) input by the user.
   * @param howMany
   *          Either the value of the <code>@</code>{@link Combine} annotation,
   *          or <code>Integer.MAX_VALUE</code>.
   * @param msg
   *          The message that triggered this command.
   * @param restriction
   *          The <code>@</code>{@link Restrict} tag passed to the parameter.
   * @return
   */
  @SuppressWarnings("unchecked")
  <E> E[] deserializeArray(Class<E> cls, ArrayList<String> values, int howMany, IMessage msg, Restrict restriction) {
    int maxSize = Math.min(values.size(), howMany);
    E[] array = (E[]) Array.newInstance(cls, maxSize);
    for (int i = 0; i < maxSize; i++) {
      array[i] = deserialize(cls, values, 1, msg, restriction);
    }
    return array;
  }
  
  /**
   * Converts an object returned by a command method to a value that can be sent
   * by the bot.
   * 
   * @param input
   *          The object returned by the command method.
   * @param msg
   *          The message that triggered the command.
   * @return The object sent to the user by the bot.
   */
  Object serialize(Object input, Class<?> type, IMessage msg) {
    Method meth = serializers.get(type);
    Object obj = serializerObjects.get(type);
    if (meth != null) {
      try {
        if (meth.getParameterCount() == 2) {
          return meth.invoke(obj, input, msg);
        } else {
          return meth.invoke(obj, input);
        }
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new SerializationException("The serialization method for type " + type.getSimpleName() + " failed.");
      }
    } else
      return input.toString();
  }
  
  /**
   * Get the <code>CommandReader</code>'s default mention setting.
   * 
   * @return The mention setting.
   */
  public MentionSetting getMentionSetting() {
    return requireMention;
  }
  
  /**
   * Get the command's mention setting as used in the command reader. This is
   * the mention setting of the command itself, unless the command uses
   * <code>DEFAULT</code>, in which case it inherits from the
   * <code>CommandReader</code>.
   * 
   * @param cmdMentionSetting
   *          The command's mention setting.
   * @return The command's mention setting, if it's not <code>DEFAULT</code>. If
   *         it is, the <code>CommandReader</code>'s mention setting.
   */
  public MentionSetting getMentionSetting(MentionSetting cmdMentionSetting) {
    if (cmdMentionSetting == MentionSetting.DEFAULT) return requireMention;
    return cmdMentionSetting;
  }
  
  /**
   * Set whether commands that inherit the <code>CommandReader</code>'s default
   * mention setting require a mention.
   * 
   * @param newSetting
   *          The new setting to use.
   */
  public void setMentionSetting(MentionSetting newSetting) {
    requireMention = newSetting;
  }
  
  /**
   * Get the prefix required for all commands.
   * 
   * @return The prefix.
   */
  public String getPrefix() {
    return prefix;
  }
  
  /**
   * Set the prefix required for all commands.
   * 
   * @param newPrefix
   *          The new prefix to use.
   */
  public void setPrefix(String newPrefix) {
    prefix = newPrefix;
  }
  
  /**
   * Sets the description used in the default Help system (above the commands).
   * 
   * @param description
   */
  public void setHelpDescription(String description) {
    helpCommand.setDescription(description);
  }
  
  /**
   * Get the {@link IDiscordClient} that is connected to the
   * <code>CommandReader</code>.
   * 
   * @return The client.
   */
  public static IDiscordClient getClient() {
    return client;
  }
}
