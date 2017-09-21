package net.nixill.commands.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import net.nixill.commands.annotations.Deserializer;
import net.nixill.commands.annotations.Restrict;
import net.nixill.commands.exceptions.DeserializationException;
import net.nixill.commands.exceptions.InvalidRestrictionError;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

/**
 * Contains default methods for (de)serializing objects and strings. Generally,
 * the (de)serializers shouldn't be called directly by bot code. However, they
 * can be overridden by registering deserializers with the same return type, or
 * serializers with the same argument type.
 * 
 * @author Nixill
 */
public final class DefaultMethods {
  /**
   * Allows selecting whether users and channels can be referenced by mention,
   * by qualified name, or both.
   * 
   * @author Nixill
   */
  public static enum MentionableMode {
    /** The user or channel can only be referenced by mention. */
    MENTION_ONLY,
    /** The user or channel can only be referenced by qualified name. */
    QUALIFIED_NAME_ONLY,
    /** The user or channel can be referenced by qualified name or mention. */
    BOTH
  };
  
  /**
   * Whether characters silently drop all but the first character of the word
   * (if true), or cause an error if there's more than one character (if false).
   */
  public static boolean                   charDropSilently = false;
  /** How users can be referenced. */
  public static MentionableMode           userMode         = MentionableMode.BOTH;
  /** How channels can be referenced. */
  public static MentionableMode           channelMode      = MentionableMode.BOTH;
  /** The strings that can be converted to booleans. */
  private static HashMap<String, Boolean> boolMap;
  
  static {
    boolMap = new HashMap<>();
    boolMap.put("0", false);
    boolMap.put("1", true);
    boolMap.put("f", false);
    boolMap.put("t", true);
    boolMap.put("false", false);
    boolMap.put("true", true);
    boolMap.put("n", false);
    boolMap.put("y", true);
    boolMap.put("no", false);
    boolMap.put("yes", true);
    boolMap.put("off", false);
    boolMap.put("on", true);
    boolMap.put("close", false);
    boolMap.put("open", true);
  }
  
  /**
   * Deserializes part of an input string into a string. Removes the portion of
   * input that was deserialized.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized String.
   */
  @Deserializer
  public static String deserializeString(ArrayList<String> values, int howMany, Restrict rest) {
    String out = "";
    int max = Math.min(values.size(), howMany);
    for (int i = 0; i < max; i++) {
      if (i != 0) out += " ";
      out += values.remove(0);
    }
    
    if (rest != null) {
      while (!out.matches(rest.value()) && !values.isEmpty()) {
        out += " " + values.remove(0);
      }
      if (!out.matches(rest.value())) { throw deserializationRestriction(out, rest); }
    }
    
    return out;
  }
  
  /**
   * Deserializes part of an input string into a primitive double. Removes the
   * portion of input that was deserialized.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized double.
   */
  @Deserializer
  public static double deserializePDouble(ArrayList<String> values, int howMany, Restrict rest) {
    String value = values.remove(0);
    try {
      double out = Double.parseDouble(value);
      if (rest == null || meetsCondition(rest.value(), out)) {
        return out;
      } else {
        throw deserializationRestriction(value, rest);
      }
    } catch (NumberFormatException ex) {
      throw new DeserializationException("Can't convert " + value + " to a number.");
    }
  }
  
  /**
   * Deserializes part of an input string into a double array. Removes the
   * portion of input that was deserialized.
   * <p>
   * This method uses the default primitive double deserializer, even if a
   * different primitive double serializer exists.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken.
   * @return The deserialized double array.
   */
  @Deserializer
  public static double[] deserializePADouble(ArrayList<String> values, int howMany, Restrict rest) {
    int max = Math.min(values.size(), howMany);
    double[] out = new double[max];
    for (int i = 0; i < max; i++) {
      out[i] = deserializePDouble(values, 1, rest);
    }
    return out;
  }
  
  /**
   * Deserializes part of an input string into a Double object. Removes the
   * portion of input that was deserialized. This method uses the default
   * primitive double deserializer, even if another primitive double
   * deserializer exists.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized Double.
   */
  @Deserializer
  public static Double deserializeDouble(ArrayList<String> values, int howMany, Restrict rest) {
    return deserializePDouble(values, howMany, rest);
  }
  
  /**
   * Deserializes part of an input string into a primitive float. Removes the
   * portion of input that was deserialized.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized float.
   */
  @Deserializer
  public static float deserializePFloat(ArrayList<String> values, int howMany, Restrict rest) {
    String value = values.remove(0);
    try {
      float out = Float.parseFloat(value);
      if (rest == null || meetsCondition(rest.value(), out)) {
        return out;
      } else {
        throw deserializationRestriction(value, rest);
      }
    } catch (NumberFormatException ex) {
      throw new DeserializationException("Can't convert " + value + " to a number.");
    }
  }
  
  /**
   * Deserializes part of an input string into a float array. Removes the
   * portion of input that was deserialized.
   * <p>
   * This method uses the default primitive float deserializer, even if a
   * different primitive float serializer exists.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken.
   * @return The deserialized float array.
   */
  @Deserializer
  public static float[] deserializePAFloat(ArrayList<String> values, int howMany, Restrict rest) {
    int max = Math.min(values.size(), howMany);
    float[] out = new float[max];
    for (int i = 0; i < max; i++) {
      out[i] = deserializePFloat(values, 1, rest);
    }
    return out;
  }
  
  /**
   * Deserializes part of an input string into a Float object. Removes the
   * portion of input that was deserialized. This method uses the default
   * primitive float deserializer, even if another primitive float deserializer
   * exists.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized Float.
   */
  @Deserializer
  public static Float deserializeFloat(ArrayList<String> values, int howMany, Restrict rest) {
    return deserializePFloat(values, howMany, rest);
  }
  
  /**
   * Deserializes part of an input string into a primitive byte. Removes the
   * portion of input that was deserialized.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized byte.
   */
  @Deserializer
  public static byte deserializePByte(ArrayList<String> values, int howMany, Restrict rest) {
    String value = values.remove(0);
    try {
      byte out = Byte.parseByte(value);
      if (rest == null || meetsCondition(rest.value(), out)) {
        return out;
      } else {
        throw deserializationRestriction(value, rest);
      }
    } catch (NumberFormatException ex) {
      throw new DeserializationException("Can't convert " + value + " to a number.");
    }
  }
  
  /**
   * Deserializes part of an input string into a byte array. Removes the
   * portion of input that was deserialized.
   * <p>
   * This method uses the default primitive byte deserializer, even if a
   * different primitive byte serializer exists.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken.
   * @return The deserialized byte array.
   */
  @Deserializer
  public static byte[] deserializePAByte(ArrayList<String> values, int howMany, Restrict rest) {
    int max = Math.min(values.size(), howMany);
    byte[] out = new byte[max];
    for (int i = 0; i < max; i++) {
      out[i] = deserializePByte(values, 1, rest);
    }
    return out;
  }
  
  /**
   * Deserializes part of an input string into a Byte object. Removes the
   * portion of input that was deserialized. This method uses the default
   * primitive byte deserializer, even if another primitive byte deserializer
   * exists.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized Byte.
   */
  @Deserializer
  public static Byte deserializeByte(ArrayList<String> values, int howMany, Restrict rest) {
    return deserializePByte(values, howMany, rest);
  }
  
  /**
   * Deserializes part of an input string into a primitive short. Removes the
   * portion of input that was deserialized.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized short.
   */
  @Deserializer
  public static short deserializePShort(ArrayList<String> values, int howMany, Restrict rest) {
    String value = values.remove(0);
    try {
      short out = Short.parseShort(value);
      if (rest == null || meetsCondition(rest.value(), out)) {
        return out;
      } else {
        throw deserializationRestriction(value, rest);
      }
    } catch (NumberFormatException ex) {
      throw new DeserializationException("Can't convert " + value + " to a number.");
    }
  }
  
  /**
   * Deserializes part of an input string into a short array. Removes the
   * portion of input that was deserialized.
   * <p>
   * This method uses the default primitive short deserializer, even if a
   * different primitive short serializer exists.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken.
   * @return The deserialized short array.
   */
  @Deserializer
  public static short[] deserializePAShort(ArrayList<String> values, int howMany, Restrict rest) {
    int max = Math.min(values.size(), howMany);
    short[] out = new short[max];
    for (int i = 0; i < max; i++) {
      out[i] = deserializePShort(values, 1, rest);
    }
    return out;
  }
  
  /**
   * Deserializes part of an input string into a Short object. Removes the
   * portion of input that was deserialized. This method uses the default
   * primitive short deserializer, even if another primitive short deserializer
   * exists.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized Short.
   */
  @Deserializer
  public static Short deserializeShort(ArrayList<String> values, int howMany, Restrict rest) {
    return deserializePShort(values, howMany, rest);
  }
  
  /**
   * Deserializes part of an input string into a primitive int. Removes the
   * portion of input that was deserialized.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized int.
   */
  @Deserializer
  public static int deserializePInteger(ArrayList<String> values, int howMany, Restrict rest) {
    String value = values.remove(0);
    try {
      int out = Integer.parseInt(value);
      if (rest == null || meetsCondition(rest.value(), out)) {
        return out;
      } else {
        throw deserializationRestriction(value, rest);
      }
    } catch (NumberFormatException ex) {
      throw new DeserializationException("Can't convert " + value + " to a number.");
    }
  }
  
  /**
   * Deserializes part of an input string into an int array. Removes the
   * portion of input that was deserialized.
   * <p>
   * This method uses the default primitive int deserializer, even if a
   * different primitive int serializer exists.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken.
   * @return The deserialized int array.
   */
  @Deserializer
  public static int[] deserializePAInteger(ArrayList<String> values, int howMany, Restrict rest) {
    int max = Math.min(values.size(), howMany);
    int[] out = new int[max];
    for (int i = 0; i < max; i++) {
      out[i] = deserializePInteger(values, 1, rest);
    }
    return out;
  }
  
  /**
   * Deserializes part of an input string into an Integer object. Removes the
   * portion of input that was deserialized. This method uses the default
   * primitive int deserializer, even if another primitive int deserializer
   * exists.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized Double.
   */
  @Deserializer
  public static Integer deserializeInteger(ArrayList<String> values, int howMany, Restrict rest) {
    return deserializePInteger(values, howMany, rest);
  }
  
  /**
   * Deserializes part of an input string into a primitive long. Removes the
   * portion of input that was deserialized.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized long.
   */
  @Deserializer
  public static long deserializePLong(ArrayList<String> values, int howMany, Restrict rest) {
    String value = values.remove(0);
    try {
      Long out = Long.parseLong(value);
      if (rest == null || meetsCondition(rest.value(), out)) {
        return out;
      } else {
        throw deserializationRestriction(value, rest);
      }
    } catch (NumberFormatException ex) {
      throw new DeserializationException("Can't convert " + value + " to a number.");
    }
  }
  
  /**
   * Deserializes part of an input string into a long array. Removes the
   * portion of input that was deserialized.
   * <p>
   * This method uses the default primitive long deserializer, even if a
   * different primitive long serializer exists.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken.
   * @return The deserialized long array.
   */
  @Deserializer
  public static long[] deserializePALong(ArrayList<String> values, int howMany, Restrict rest) {
    int max = Math.min(values.size(), howMany);
    long[] out = new long[max];
    for (int i = 0; i < max; i++) {
      out[i] = deserializePLong(values, 1, rest);
    }
    return out;
  }
  
  /**
   * Deserializes part of an input string into a Long object. Removes the
   * portion of input that was deserialized. This method uses the default
   * primitive long deserializer, even if another primitive long deserializer
   * exists.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized Double.
   */
  @Deserializer
  public static Long deserializeLong(ArrayList<String> values, int howMany, Restrict rest) {
    return deserializePLong(values, howMany, rest);
  }
  
  /**
   * Deserializes part of an input string into a primitive boolean. Removes the
   * portion of input that was deserialized.
   * <p>
   * Specifically, this method takes the input, converts it to lowercase, and
   * matches it to a value in the internal boolean map, returning that value or
   * throwing a {@link DeserializationException} if there is no match.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized boolean.
   */
  @Deserializer
  public static boolean deserializePBoolean(ArrayList<String> values, int howMany) {
    String value = values.remove(0);
    String valueLC = value.toLowerCase();
    Boolean bool = boolMap.get(valueLC);
    if (bool == null) throw new DeserializationException("Can't convert " + value + " to a boolean.");
    return bool;
  }
  
  /**
   * Adds a word-to-boolean mapping to the internal table. The specified word
   * will be converted to lowercase internally.
   * 
   * @param word
   *          The word to add.
   * @param value
   *          The value to return for that word.
   * @return The previous value for that word, if there was any.
   */
  public static Boolean addMapping(String word, boolean value) {
    word = word.toLowerCase();
    return boolMap.put(word, value);
  }
  
  /**
   * Removes a word-to-boolean mapping from the internal table. The specified
   * word will be converted to lowercase internally.
   * 
   * @param word
   *          The word to remove.
   * @return The value for that word, if there was any.
   */
  public static Boolean delMapping(String word) {
    word = word.toLowerCase();
    return boolMap.remove(word);
  }
  
  /**
   * Adds word-to-boolean mappings from a specified map to the internal table.
   * All words will be converted to lowercase internally, and mappings with
   * spaces in the word will be skipped.
   * 
   * @param map
   *          The map to copy mappings from.
   */
  public static void addMappings(Map<? extends String, ? extends Boolean> map) {
    for (String key : map.keySet()) {
      if (!key.contains(" ")) addMapping(key, map.get(key));
    }
  }
  
  /**
   * Adds word-to-boolean mappings from (space-separated) lists of false and
   * true words.
   * 
   * @param falseWords
   *          A string composed of space-separated words that should map to
   *          false.
   * @param trueWords
   *          A string composed of space-separated words that should map to
   *          true.
   */
  public static void addMappings(String falseWords, String trueWords) {
    for (String word : falseWords.split(" ")) {
      addMapping(word, false);
    }
    for (String word : trueWords.split(" ")) {
      addMapping(word, true);
    }
  }
  
  /**
   * Clears the internal table, then copies the new values from the specified
   * map.
   * 
   * @param map
   *          The map to copy new mappings from.
   */
  public static void setMappings(Map<? extends String, ? extends Boolean> map) {
    clearMappings();
    addMappings(map);
  }
  
  /**
   * Clears the internal table, then copies the new values from the specified
   * strings.
   * 
   * @param falseWords
   *          A string composed of space-separated words that should map to
   *          false.
   * @param trueWords
   *          A string composed of space-separated words that should map to
   *          true.
   */
  public static void setMappings(String falseWords, String trueWords) {
    clearMappings();
    addMappings(falseWords, trueWords);
  }
  
  /**
   * Removes all mappings from the internal table.
   */
  public static void clearMappings() {
    boolMap.clear();
  }
  
  /**
   * Deserializes part of an input string into a boolean array. Removes the
   * portion of input that was deserialized.
   * <p>
   * This method uses the default primitive boolean deserializer, even if a
   * different primitive boolean serializer exists.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken.
   * @return The deserialized boolean array.
   */
  @Deserializer
  public static boolean[] deserializePABoolean(ArrayList<String> values, int howMany) {
    int max = Math.min(values.size(), howMany);
    boolean[] out = new boolean[max];
    for (int i = 0; i < max; i++) {
      out[i] = deserializePBoolean(values, 1);
    }
    return out;
  }
  
  /**
   * Deserializes part of an input string into a Boolean object. Removes the
   * portion of input that was deserialized. This method uses the default
   * primitive boolean deserializer, even if another primitive boolean
   * deserializer exists.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized Double.
   */
  @Deserializer
  public static Boolean deserializeBoolean(ArrayList<String> values, int howMany) {
    return deserializePBoolean(values, howMany);
  }
  
  /**
   * Deserializes part of an input string into a primitive char. Removes the
   * portion of input that was deserialized.
   * <p>
   * Specifically, this method will first attempt to turn certain words into
   * specific characters. If that fails, and {@link #charDropSilently} is true,
   * the first character of the input is returned. Otherwise, an error is thrown
   * unless the input is exactly one character.
   * <p>
   * The keywords are as follows:
   * <table>
   * <tr>
   * <th>Keyword</th>
   * <th>Value</th>
   * </tr>
   * <tr>
   * <td>(empty), sp, space, \s</td>
   * <td>A space character</td>
   * </tr>
   * <tr>
   * <td>nl, newline, new_line, \n</td>
   * <td>A newline character (\n)</td>
   * </tr>
   * <tr>
   * <td>re, return, \r</td>
   * <td>A return character (\r)</td>
   * </tr>
   * <tr>
   * <td>tab, \t</td>
   * <td>A tab character (\t)</td>
   * </tr>
   * <tr>
   * <td>\\</td>
   * <td>A backslash (\)</td>
   * </tr>
   * </table>
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized char.
   */
  @Deserializer
  public static char deserializePCharacter(ArrayList<String> values, int howMany, Restrict rest) {
    String value = values.remove(0);
    String valueLC = value.toLowerCase();
    char out = '\0';
    switch (valueLC) {
      case "":
      case "sp":
      case "space":
      case "\\s":
        out = ' ';
        break;
      case "nl":
      case "newline":
      case "new_line":
      case "\\n":
        out = '\n';
        break;
      case "re":
      case "return":
      case "\\r":
        out = '\r';
        break;
      case "tab":
      case "\\t":
        out = '\t';
        break;
      case "\\\\":
        out = '\\';
        break;
      default:
        if (value.length() > 1 && !charDropSilently)
          throw new DeserializationException("\"" + value + "\" is not a valid character.");
        out = value.charAt(0);
    }
    if (rest == null || String.valueOf(out).matches("[" + rest + "]"))
      return out;
    else
      throw deserializationRestriction(value, rest);
  }
  
  /**
   * Deserializes part of an input string into a char array. Removes the
   * portion of input that was deserialized.
   * <p>
   * This deserializer works by first converting escape sequences (\\, \n, \r,
   * \t, \s) into single characters (\, new line, carriage return, tab, space).
   * Then it converts the string(s, space separated) into an array of
   * characters.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken.
   * @return The deserialized double array.
   */
  @Deserializer
  public static char[] deserializePACharacter(ArrayList<String> values, int howMany, Restrict rest) {
    int max = Math.min(values.size(), howMany);
    String toArray = "";
    for (int i = 0; i < max; i++) {
      if (i != 0) toArray += " ";
      toArray += values.remove(0);
    }
    toArray.replaceAll("\\\\\\\\", "\ue000");
    toArray.replaceAll("\\\\n", "\n");
    toArray.replaceAll("\\\\r", "\r");
    toArray.replaceAll("\\\\t", "\t");
    toArray.replaceAll("\\\\s", " ");
    toArray.replaceAll("\\ue000", "\\");
    if (rest == null || toArray.matches(rest.value()))
      return toArray.toCharArray();
    else
      throw new DeserializationException("\"" + toArray + "\" is not a valid input.");
  }
  
  /**
   * Deserializes part of an input string into a Character object. Removes the
   * portion of input that was deserialized. This method uses the default
   * primitive char deserializer, even if another primitive char deserializer
   * exists.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized Double.
   */
  @Deserializer
  public static Character deserializeCharacter(ArrayList<String> values, int howMany, Restrict rest) {
    return deserializePCharacter(values, howMany, rest);
  }
  
  /**
   * Deserializes part of an input string into an {@link IUser} object. Removes
   * the portion of input that was deserialized.
   * <p>
   * A user can only be referenced by @mention at this time.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized IUser.
   */
  @Deserializer
  public static IUser deserializeUser(ArrayList<String> values, int howMany) {
    String value = values.remove(0);
    Matcher mtc = Pattern.compile("<@!?(\\d+)>").matcher(value);
    if (mtc.matches()) {
      IUser out = CommandReader.getClient().fetchUser(Long.parseLong(mtc.group(1)));
      if (out != null) return out;
      throw new DeserializationException("Can't get user " + value + "; perhaps they don't exist?");
    }
    throw new DeserializationException("It would appear " + value + " is not a user mention.");
  }
  
  /**
   * Deserializes part of an input string into an {@link IChannel} object.
   * Removes the portion of input that was deserialized.
   * <p>
   * A channel can only be referenced by @mention at this time.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized IChannel.
   */
  @Deserializer
  public static IChannel deserializeChannel(ArrayList<String> values, int howMany) {
    String value = values.remove(0);
    Matcher mtc = Pattern.compile("<#(\\d+)>").matcher(value);
    if (mtc.matches()) {
      IChannel out = CommandReader.getClient().getChannelByID(Long.parseLong(mtc.group(1)));
      if (out != null) return out;
      throw new DeserializationException(
          "Can't get channel " + value + "; perhaps it doesn't exist or I can't access it?");
    }
    throw new DeserializationException("It would appear " + value + " is not a channel mention.");
  }
  
  /**
   * Deserializes part of an input string into an {@link IRole} object. Removes
   * the portion of input that was deserialized.
   * <p>
   * A role can only be referenced by @mention at this time.
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized IRole.
   */
  @Deserializer
  public static IRole deserializeRole(ArrayList<String> values, int howMany) {
    String value = values.remove(0);
    Matcher mtc = Pattern.compile("<@&(\\d+)>").matcher(value);
    if (mtc.matches()) {
      IRole out = CommandReader.getClient().getRoleByID(Long.parseLong(mtc.group(1)));
      if (out != null) return out;
      throw new DeserializationException("Can't get role " + value + "; perhaps they don't exist?");
    }
    throw new DeserializationException("It would appear " + value + " is not a role mention.");
  }
  
  /**
   * Deserializes part of an input string into an {@link Emoji} object. Removes
   * the portion of input that was deserialized.
   * <p>
   * An emoji can be referenced using any of the following methods:
   * <ul>
   * <li>Actually send the emoji</li>
   * <li>Name the emoji</li>
   * </ul>
   * 
   * @param values
   *          The remaining values. Values used for deserialization should be
   *          removed from the original list, and should be removed from the
   *          front.
   * @param howMany
   *          How many values should be taken, if it makes sense to take
   *          multiple values.
   * @return The deserialized Emoji.
   */
  @Deserializer
  public static Emoji deserializeEmoji(ArrayList<String> values, int howMany) {
    String value = values.remove(0);
    String valueOrig = value;
    if (value.startsWith(":") && value.endsWith(":")) value = value.substring(1, value.length() - 1).toLowerCase();
    Emoji emoji = EmojiManager.getByUnicode(value);
    if (emoji != null) return emoji;
    emoji = EmojiManager.getByUnicode(value.substring(0, 1));
    if (emoji != null) return emoji;
    emoji = EmojiManager.getForAlias(value);
    if (emoji != null) return emoji;
    throw new DeserializationException(valueOrig + " is not a custom emoji.");
  }
  
  @Deserializer
  public static Matcher deserializeMatcher(ArrayList<String> values, int howMany, Restrict rest) {
    if (rest == null)
      throw new InvalidRestrictionError("Matcher parameters *must* have a regex @Restrict to match against.");
    String value = deserializeString(values, howMany, null);
    
    while (!value.matches(rest.value()) && !values.isEmpty()) {
      value += " " + values.remove(0);
    }
    
    Matcher mtc = Pattern.compile(rest.value()).matcher(value);
    
    if (!mtc.matches()) { throw deserializationRestriction(value, rest); }
    
    return mtc;
  }
  
  private static boolean meetsCondition(String condition, long in) {
    condition = condition.replaceAll("[ \\t\\n\\r]", "");
    String[] ands = condition.split("&");
    for (String and : ands) {
      String[] ors = and.split("\\|");
      boolean satisfiedInner = false;
      for (String or : ors) {
        String kw = or.toLowerCase().replaceAll("\\-", "");
        switch (kw) {
          case "positive":
            or = ">0";
            break;
          case "negative":
            or = "<0";
            break;
          case "nonpositive":
            or = "<=0";
            break;
          case "nonnegative":
            or = ">=0";
            break;
          case "even":
            or = "%2";
            break;
          case "odd":
            or = "!%2";
            break;
        }
        
        Matcher mtc = Pattern.compile("(!?)([><]?=|[><]|\\^|%)(-?\\d+)").matcher(or);
        if (!mtc.matches()) throw new InvalidRestrictionError("The condition " + or + " is not valid.");
        
        long number = Long.parseLong(mtc.group(3));
        boolean invert = mtc.group(1).matches("!");
        String cond = mtc.group(2);
        boolean satisfy = false;
        
        if (cond.equals(">="))
          satisfy = (in >= number);
        else if (cond.equals("<="))
          satisfy = (in <= number);
        else if (cond.equals(">"))
          satisfy = (in > number);
        else if (cond.equals("<"))
          satisfy = (in < number);
        else if (cond.equals("="))
          satisfy = (in == number);
        else if (cond.equals("%"))
          satisfy = (in % number == 0);
        else if (cond.equals("^")) {
          double log = Math.log(in) / Math.log(number);
          satisfy = (log == Math.floor(log));
        }
        
        satisfy = (satisfy != invert);
        
        satisfiedInner = satisfiedInner | satisfy;
      }
      if (satisfiedInner == false) return false;
    }
    return true;
  }
  
  private static boolean meetsCondition(String condition, double in) {
    condition = condition.replaceAll("[ \\t\\n\\r\\-]", "");
    String[] ands = condition.split("&");
    for (String and : ands) {
      String[] ors = and.split("\\|");
      boolean satisfiedInner = false;
      for (String or : ors) {
        String kw = or.toLowerCase();
        switch (kw) {
          case "positive":
            or = ">0";
            break;
          case "negative":
            or = "<0";
            break;
          case "nonpositive":
            or = "<=0";
            break;
          case "nonnegative":
            or = ">=0";
            break;
          case "even":
            or = "%2";
            break;
          case "odd":
            or = "!%2";
            break;
        }
        
        Matcher mtc = Pattern.compile("(!?)([><]?=|[><]|\\^|%)(-?\\d*\\.\\d+)").matcher(or);
        if (!mtc.matches()) throw new InvalidRestrictionError("The condition " + or + " is not valid.");
        
        double number = Double.parseDouble(mtc.group(3));
        boolean invert = mtc.group(1).matches("!");
        String cond = mtc.group(2);
        boolean satisfy = false;
        
        if (cond.equals(">="))
          satisfy = (in >= number);
        else if (cond.equals("<="))
          satisfy = (in <= number);
        else if (cond.equals(">"))
          satisfy = (in > number);
        else if (cond.equals("<"))
          satisfy = (in < number);
        else if (cond.equals("="))
          satisfy = (in == number);
        else if (cond.equals("%"))
          satisfy = (in % number == 0);
        else if (cond.equals("^")) {
          double log = Math.log(in) / Math.log(number);
          satisfy = (log == Math.floor(log));
        }
        
        satisfy = (satisfy != invert);
        
        satisfiedInner = satisfiedInner | satisfy;
      }
      if (satisfiedInner == false) return false;
    }
    return true;
  }
  
  private static DeserializationException deserializationRestriction(Object out, Restrict rest) {
    String errText = rest.error();
    if (errText.isEmpty()) {
      return new DeserializationException(out.toString() + " does not meet the restriction.", true);
    } else {
      return new DeserializationException(errText.replace("{INPUT}", out.toString()));
    }
  }
}
