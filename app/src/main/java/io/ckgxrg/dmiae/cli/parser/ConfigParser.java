package io.ckgxrg.dmiae.cli.parser;

import io.ckgxrg.dmiae.data.Character;
import java.lang.reflect.Field;
import java.util.Optional;

/** Provides some utilities parsing config flags. */
public class ConfigParser {
  /** Called if a character declaration is read. */
  protected static Character identifyCharacter(String s) {
    String[] ss = s.split(":");
    String desc = "";
    if (ss.length != 1) {
      desc = ss[1];
    }
    String[] name = ss[0].split(",");
    Character c = new Character(name);
    c.addDesc(desc);
    if (Parser.INSTANCE.verbose) {
      System.out.println("Character: @" + s);
    }
    return c;
  }

  /** Called when a parser flag statement is read. */
  protected static void parseFlag(String s) {
    String key = s.split(":")[0].toLowerCase().strip();
    String value = s.split(":")[1].strip();
    try {
      if (Parser.INSTANCE.verbose) {
        System.out.println("Property #" + key + " set to " + value);
      }
      Field f = Parser.INSTANCE.flags.getClass().getDeclaredField(key);
      f.set(Parser.INSTANCE.flags, Optional.of(value));
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
}
