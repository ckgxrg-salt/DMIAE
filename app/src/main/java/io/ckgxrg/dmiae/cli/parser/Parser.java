package io.ckgxrg.dmiae.cli.parser;

import io.ckgxrg.dmiae.data.Annotation;
import io.ckgxrg.dmiae.data.AnnotationType;
import io.ckgxrg.dmiae.data.Character;
import io.ckgxrg.dmiae.data.Line;
import io.ckgxrg.dmiae.data.Script;
import io.ckgxrg.dmiae.data.Subline;
import io.ckgxrg.dmiae.util.TextUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

/**
 * The Drama.Interactive resolver. Intended to be called as $dmiae resolve raw file. Reads
 * instructions and lines from raw DMIAE formatted text files.
 */
public class Parser implements Runnable {

  // Source of the file
  File src;
  String configSrc;
  String contentSrc;

  // Markers for current line
  Script generated;
  String currentLine;
  HashSet<Character> currentCharacter;

  // Runtime
  public static Parser INSTANCE;
  public Thread thread;
  public Flags flags;

  // Sublines and Annotations that are queued to be applied
  public ArrayList<Subline> heldSublines;
  public ArrayList<Annotation> heldAnnos;

  /**
   * Starts the Revolver.
   *
   * @param file Path to the script file
   */
  public void begin(String file) {
    begin(new File(file));
  }

  /**
   * Starts the Revolver.
   *
   * @param file Path to the script file
   */
  public void begin(File file) {
    src = file;
    thread = new Thread(this, "dmiae-resolver");
    thread.start();
  }

  /** The thread main method. Do not invoke manually, use @see Resolver.start() instead. */
  @Override
  public void run() {
    INSTANCE = this;
    System.out.println("|==========PARSER====STARTS==========|");
    overview();
    parseConfig();
    applySublineFormat();
    System.out.println("|==========READ==LINES==BEGIN==========|");
    // Default to the fallback character
    currentCharacter.add(Character.Everyone);
    readLine();
    System.out.println("|=========READ==LINES==COMPLETE=========|");
    try (ObjectOutputStream obj = new ObjectOutputStream(new FileOutputStream("dmiae.out"))) {
      obj.writeObject(generated);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  void overview() {
    try (BufferedReader br = new BufferedReader(new FileReader(src))) {
      System.out.println("===>[Parser] Evaluating file: " + src.toString());
      String line;
      StringBuilder cache = new StringBuilder();
      // Read the header section
      while ((line = br.readLine()) != null) {
        line = line.strip();
        if (line.isBlank()) {
          continue;
        }
        if (line.equals("#END")) {
          configSrc = cache.toString();
          cache = new StringBuilder();
          continue;
        }
        cache.append(line + "\n");
      }
      contentSrc = cache.toString();
      // Validate
      if (configSrc.isEmpty()) {
        System.err.println("===>[Parser] Cannot make much sense, no header section found.");
        System.exit(1);
      }
      if (contentSrc.isEmpty()) {
        System.err.println("===>[Parser] Cannot make much sense, no content section found.");
        System.exit(1);
      }
    } catch (IOException io) {
      System.err.println("===>[Parser] Unexpected IO error");
      io.printStackTrace();
    }
  }

  /** Read instructions from the headers. */
  void parseConfig() {
    flags = new Flags();
    generated = new Script();
    currentCharacter = new HashSet<Character>();
    try (BufferedReader br = new BufferedReader(new StringReader(configSrc))) {
      while ((currentLine = br.readLine()) != null) {
        if (currentLine.startsWith("@")) {
          Character c = ConfigParser.identifyCharacter(currentLine.substring(1));
          generated.characters.add(c);
        } else if (currentLine.startsWith("#")) {
          ConfigParser.parseFlag(currentLine.substring(1));
        }
      }
    } catch (IOException io) {
      io.printStackTrace();
    }
  }

  /** Generate subline markers defined by #sublineFormat. */
  void applySublineFormat() {
    String[] split = flags.sublineformat.orElse("0,0").split(",");
    if (split.length != 2) {
      System.err.println("===>[Parser] Value of #sublineFormat is invalid, skipping...");
      return;
    }
    try (BufferedReader br = new BufferedReader(new StringReader(contentSrc))) {
      int prevCount = Integer.valueOf(split[0].strip());
      int afterCount = Integer.valueOf(split[1].strip());
      int p = 0;
      int a = 0;
      boolean prev = true;
      StringBuilder cache = new StringBuilder();
      while ((currentLine = br.readLine()) != null) {
        if (prev && p < prevCount) {
          cache.append(">:" + currentLine + "\n");
          p++;
        } else if (!prev && a < afterCount) {
          cache.append("<:" + currentLine + "\n");
        } else if (p == prevCount || a == afterCount) {
          prev = !prev;
          cache.append(currentLine + "\n");
        }
      }
      contentSrc = cache.toString();
    } catch (NumberFormatException e) {
      System.err.println("===>[Parser] Value of #sublineFormat is invalid, skipping...");
      e.printStackTrace();
      return;
    } catch (IOException e) {
      System.err.println("===>[Parser] Unexpected error");
      e.printStackTrace();
    }
  }

  /** Read actual lines, sublines and annotations. */
  void readLine() {
    heldAnnos = new ArrayList<Annotation>();
    heldSublines = new ArrayList<Subline>();
    try (BufferedReader br = new BufferedReader(new StringReader(contentSrc))) {
      while ((currentLine = br.readLine()) != null) {
        // Try to update current character
        updateChara(currentLine);
        if (currentLine.isBlank()) {
          continue;
        }

        // Forced read main line
        if (currentLine.startsWith("::")) {
          currentLine = TextUtils.untilLetter(currentLine);
          readMainLine();
          continue;
        }

        // Annotation and Subline detection
        if (readAnno()) {
          continue;
        }
        if (readSubline()) {
          continue;
        }
        readMainLine();
      }
    } catch (IOException io) {
      io.printStackTrace();
    }
  }

  /**
   * Attempt to match the known names of Characters to look for Character lines. Will remove the
   * character name if detected.
   */
  void updateChara(String s) {
    // First check if this line has multiple characters
    if (s.contains("/")) {
      updateMultiChara(s);
      return;
    }

    // Try the declared characters
    for (Character c : generated.characters) {
      for (String ss : c.names) {
        if (s.startsWith(ss) && checkAmbiguity(currentLine, ss)) {
          currentLine = TextUtils.rmName(currentLine, ss);
          currentLine = currentLine.strip();
          currentCharacter.clear();
          currentCharacter.add(c);
        }
      }
    }

    // Try the fallback characters
    for (Character c : Character.fallbacks) {
      for (String ss : c.names) {
        if (s.startsWith(ss)) {
          currentLine = TextUtils.rmName(currentLine, ss);
          currentLine = currentLine.strip();
          currentCharacter.clear();
          currentCharacter.add(c);
        }
      }
    }
  }

  /** The case if there are multiple characters in a single line. */
  void updateMultiChara(String s) {
    String[] names = s.split("/");
    HashSet<Character> chs = new HashSet<Character>();
    // There should be at least 1 character
    int expect = 1;
    String last = "";
    for (String d : names) {
      // Each found name might mean an extra character, or possibly just alias of an existing
      // character
      expect++;
      d = d.strip();
      // Try the declared characters
      for (Character c : generated.characters) {
        for (String n : c.names) {
          if (n.equals(d)) {
            if (chs.contains(c)) {
              // If the character already exists, we do not expect a new character to be there
              expect--;
            }
            chs.add(c);
            break;
          }
        }
      }

      // Try the fallback characters
      for (Character c : Character.fallbacks) {
        for (String n : c.names) {
          if (n.equals(d)) {
            if (chs.contains(c)) {
              expect--;
            }
            chs.add(c);
            break;
          }
        }
      }
      last = d;

      // Break if the character count cannot fulfill expectation, e.g. used undefined characters
      if (chs.size() < expect) {
        break;
      }
    }
    // Do not update if no characters spotted
    if (chs.isEmpty()) {
      return;
    }
    currentCharacter.clear();
    currentCharacter = chs;
    String[] tmp = currentLine.split(last);
    currentLine = tmp.length == 1 ? "" : tmp[1];
    currentLine = TextUtils.untilLetter(currentLine);
    currentLine = currentLine.strip();
  }

  /**
   * Handle Annotations.
   *
   * @return Whether this line is an annotation or not
   */
  boolean readAnno() {
    if (currentLine.startsWith("@") || currentLine.startsWith(">@")) {
      heldAnnos.add(
          new Annotation(
              TextUtils.getAnnoContent(currentLine), TextUtils.identifyAnnoType(currentLine)));
      return true;
    } else if (currentLine.startsWith("<@")) {
      AnnotationType t = TextUtils.identifyAnnoType(currentLine);
      Annotation a = new Annotation(TextUtils.getAnnoContent(currentLine), t);
      a.parent = generated.lines.getLast();
      generated.lines.getLast().addAnnotationAfter(a);
      System.out.println("^===" + t.toString() + ": " + TextUtils.getAnnoContent(currentLine));
      return true;
    }
    return false;
  }

  /** Handle Sublines. */
  boolean readSubline() {
    if (currentLine.startsWith(">:")) {
      currentLine = TextUtils.rmColon(currentLine);
      updateChara(currentLine);
      heldSublines.add(new Subline(currentLine, currentCharacter));
      return true;
    } else if (currentLine.startsWith(":") || currentLine.startsWith("<:")) {
      currentLine = TextUtils.rmColon(currentLine);
      updateChara(currentLine);
      Subline s = new Subline(currentLine, currentCharacter);
      s.parent = generated.lines.getLast();
      generated.lines.getLast().addSublineAfter(s);
      System.out.println("\t" + currentLine);
      return true;
    }
    return false;
  }

  /** Finally it could be an actual line. */
  void readMainLine() {
    Line l = new Line(currentLine, currentCharacter);
    // Apply the cached Sublines and Annotations
    for (Annotation a : heldAnnos) {
      a.parent = l;
      l.addAnnotationBefore(a);
      System.out.println("V===" + a.type.toString() + ": " + a.content);
    }
    for (Subline s : heldSublines) {
      s.characters = l.characters;
      l.addSublineBefore(s);
      System.out.println("V\t" + currentLine);
    }
    heldAnnos.clear();
    heldSublines.clear();
    generated.lines.add(l);
    System.out.println(TextUtils.getCharaName(currentCharacter) + ": " + currentLine);
  }

  /**
   * Intercepts the character identification if the Character's name is just separated with a single
   * space, which is ambiguous.
   *
   * @param line The current line
   * @param name The character name
   * @return Whether this should be intercepted
   */
  boolean checkAmbiguity(String line, String name) {
    if (line.split(name).length > 1 && line.split(name)[1].startsWith(" ")) {
      if (line.split(name)[1].isBlank()) {
        return true;
      }
      System.out.println("===>[Parser] Ambiguity detected at: " + line);
      try (Scanner sc = new Scanner(System.in)) {
        System.out.print("===>[Parser] Should this line be " + name + "'s? (y/N):");
        String ss = sc.nextLine();
        return ss.equalsIgnoreCase("y");
      }
    }
    return true;
  }
}
