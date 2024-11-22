package io.ckgxrg.dmiae.cli.parser;

import io.ckgxrg.dmiae.data.Character;
import io.ckgxrg.dmiae.data.ScriptProps;
import java.lang.reflect.Field;
import java.util.Optional;

/** Provides some utilities parsing config flags. */
public class ConfigParser {
  /** Called if a character declaration is read. */
  protected static Character identifyCharacter(String s, boolean verbose) {
    String[] ss = s.split(":");
    String desc = "";
    if (ss.length != 1) {
      desc = ss[1];
    }
    String[] name = ss[0].split(",");
    Character c = new Character(name);
    c.addDesc(desc);
    if (verbose) {
      System.out.println("Character: @" + s);
    }
    return c;
  }

  /** Called when a parser flag statement is read. */
  protected static void parseProp(String s, ScriptProps props, boolean verbose) {
    String key = s.split(":")[0].toLowerCase().strip();
    String value = s.split(":")[1].strip();
    try {
      if (verbose) {
        System.out.println("Script property #" + key + " set to " + value);
      }
      Field f = props.getClass().getDeclaredField(key);
      f.set(props, Optional.of(value));
      f.setAccessible(true);
    } catch (NoSuchFieldException e) {
      System.out.println("===>[Parser] The property " + key + " is unknown, skipping...");
    } catch (SecurityException e) {
      System.out.println("===>[Parser] Unexpected error");
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      System.out.println(
          "===>[Parser] Value for the property " + key + " has wrong type, skipping...");
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      System.out.println("[DMIAE] Unexpected error");
      e.printStackTrace();
    }
  }

  /** Called when a parser flag statement is read. */
  protected static void parseFlag(String s, Flags flags) {
    String key = s.split(":")[0].toLowerCase().strip();
    String value = s.split(":")[1].strip();
    try {
      if (Parser.INSTANCE.verbose) {
        System.out.println("Flag " + key + " set to " + value);
      }
      Field f = flags.getClass().getDeclaredField(key);
      f.set(flags, Optional.of(value));
      f.setAccessible(true);
    } catch (NoSuchFieldException e) {
      System.out.println("===>[Parser] The flag " + key + " is unknown, skipping...");
    } catch (SecurityException e) {
      System.out.println("===>[Parser] Unexpected error");
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      System.out.println("===>[Parser] Value for the flag " + key + " has wrong type, skipping...");
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      System.out.println("[DMIAE] Unexpected error");
      e.printStackTrace();
    }
  }
}
