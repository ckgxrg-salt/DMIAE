package io.ckgxrg.dmiae.cli.parser;

import io.ckgxrg.dmiae.data.AnnotationType;
import java.util.Optional;

/** All possibilities for a raw text line. */
public class RawLine {

  /** Just for returning all these types. */
  static interface Export {}

  /** This line supplies info for the script. */
  static class Cfg implements Export {
    String key;
    String value;

    public Cfg(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }

  /** This line declares a character. */
  static class Chara implements Export {
    String[] names;
    Optional<String> desc;

    public Chara(String... names) {
      this.names = names;
      this.desc = Optional.empty();
    }

    public Chara(String desc, String... names) {
      this.names = names;
      this.desc = Optional.of(desc);
    }
  }

  /** This line is said by a character. */
  static class Lne implements Export {
    Character[] charas;
    Optional<String> content;

    public Lne(Character[] charas) {
      this.charas = charas;
      this.content = Optional.empty();
    }

    public Lne(Character[] charas, String content) {
      this.charas = charas;
      this.content = Optional.of(content);
    }
  }

  /** This line is supplementary to another line. */
  static class Sub implements Export {
    String content;

    public Sub(String content) {
      this.content = content;
    }
  }

  /** This line annotates another line. */
  static class Anno implements Export {
    AnnotationType type;
    String content;

    public Anno(AnnotationType type, String content) {
      this.type = type;
      this.content = content;
    }
  }

  /** This line commands the DMIAE parser. */
  static class Flag implements Export {
    String key;
    String value;
    Optional<FlagDomain> dom;

    public Flag(String key, String value) {
      this.key = key;
      this.value = value;
      this.dom = Optional.empty();
    }

    public Flag(String key, String value, FlagDomain dom) {
      this.key = key;
      this.value = value;
      this.dom = Optional.of(dom);
    }
  }

  /**
   * Identifies which type this line is, extracting the info and send them back. Please trim the
   * line before invocation.
   *
   * @param line The line to work on.
   * @param configPhase Whether the parser is still in config phase.
   * @return One above wrapper class containing the information.
   */
  public static Export unpack(String line, boolean configPhase) throws FormatException {
    // Config or Flags
    if (line.startsWith("#")) {
      String key;
      String value;
      String[] split = line.split(":");
      if (split.length != 2) {
        throw new FormatException("Config or Flag should be a key-value pair");
      }
      key = split[0].trim().substring(1);
      value = split[1].trim();
      if (configPhase) {
        return new Cfg(key, value);
      } else {
        return new Flag(key, value);
      }
    }
    // Characters
    if (configPhase && line.startsWith("@")) {
      String[] split = line.split(":");
      String[] names = split[0].substring(1).split(",");
      if (split.length == 2) {
        String desc = split[1].trim();
        return new Chara(desc, names);
      } else {
        return new Chara(names);
      }
    }

    // Annotations
    if (line.startsWith("@") || line.startsWith("<@") || line.startsWith(">@")) {
      AnnotationType type;
      String content;
      String[] split = line.split(":");
      if (split.length != 2) {
        throw new FormatException("Bad Annotation format");
      }
      if (split[0].trim().endsWith("@")) {
        type = AnnotationType.NOTE;
      } else {
        switch (split[0].trim().toUpperCase()) {
          case "@LIGHTING", "@LIGHT", "@LT", "@L":
            type = AnnotationType.LIGHTING;
            break;
          case "@AUDIO", "@MUSIC", "@SOUND", "@A":
            type = AnnotationType.AUDIO;
            break;
          case "@CHARACTER", "@CHARA", "@CH", "@C":
            type = AnnotationType.CHARACTER;
            break;
          case "@CMD":
            type = AnnotationType.COMMAND;
            break;
          default:
            type = AnnotationType.NOTE;
        }
      }
      content = split[1].trim();
      return new Anno(type, content);
    }
    // Sublines
    if (line.startsWith(":") || line.startsWith("<:") || line.startsWith(">:")) {
      String content;
      if (line.endsWith(":")) {
        throw new FormatException("Sublines must have content");
      }
      content = line.substring(line.indexOf(":"));
      return new Sub(content);
    }
    throw new FormatException("Format does not match any");
  }
}
