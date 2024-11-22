package io.ckgxrg.dmiae.cli.parser;

import io.ckgxrg.dmiae.cli.DramaInteractive;
import io.ckgxrg.dmiae.data.Annotation;
import io.ckgxrg.dmiae.data.AnnotationType;
import io.ckgxrg.dmiae.data.Character;
import io.ckgxrg.dmiae.data.Line;
import io.ckgxrg.dmiae.data.Script;
import io.ckgxrg.dmiae.data.Subline;
import io.ckgxrg.dmiae.exceptions.FormatException;
import io.ckgxrg.dmiae.util.TextUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * The Drama.Interactive resolver. Intended to be called as $dmiae resolve raw file. Reads
 * instructions and lines from raw DMIAE formatted text files.
 */
public class Parser implements Callable<Optional<Script>> {

  // Source of the file
  File src;
  ArrayList<String> configSrc;
  ArrayList<String> contentSrc;

  // Markers for current line
  Script generated;
  HashSet<Character> currentCharacter;
  String currentLine;
  int currentIndex;

  // Runtime
  public static Parser INSTANCE;
  public boolean verbose;

  // Sublines and Annotations that are queued to be applied
  public ArrayList<Subline> heldSublines;
  public ArrayList<Annotation> heldAnnos;

  // Flags area
  public String sublineformat;

  /**
   * Starts the Revolver.
   *
   * @param file Path to the script file
   */
  public Parser(File file) {
    this(file, false);
  }

  /**
   * Starts the Revolver.
   *
   * @param file Path to the script file
   */
  public Parser(File file, boolean verbose) {
    src = file;
    this.verbose = verbose;
  }

  /** The thread main method. Do not invoke manually, use @see Resolver.start() instead. */
  @Override
  public Optional<Script> call() {
    INSTANCE = this;
    System.out.println("===>[Parser] Started");
    overview();
    if (Thread.currentThread().isInterrupted()) {
      return Optional.empty();
    }
    parseProps();
    Preprocess.applySublineFormat(
        flags.sublineformat.orElse(Flags.DEFAULT_SUBLINEFORMAT), contentSrc);
    System.out.println("===>[Parser] Begin reading lines");
    flags = new Flags();
    // Default to the fallback character
    currentCharacter.add(Character.Everyone);
    readLine();
    System.out.println("===>[Parser] Finished");
    return Optional.of(generated);
  }

  // Internal zone

  void overview() {
    configSrc = new ArrayList<String>();
    contentSrc = new ArrayList<String>();
    boolean config = true;
    try (BufferedReader br = new BufferedReader(new FileReader(src))) {
      System.out.println("===>[Parser] Evaluating file: " + src.toString());
      String line;
      // Read the header section
      while ((line = br.readLine()) != null) {
        line = line.strip();
        if (line.isBlank()) {
          continue;
        }
        if (line.equals("#END")) {
          config = false;
          continue;
        }
        if (config) {
          configSrc.add(line);
        } else {
          contentSrc.add(line);
        }
      }
      // Validate
      if (configSrc.isEmpty()) {
        System.err.println("===>[Parser] Cannot make much sense, no header section found.");
        Thread.currentThread().interrupt();
      }
      if (contentSrc.isEmpty()) {
        System.err.println("===>[Parser] Cannot make much sense, no content section found.");
        Thread.currentThread().interrupt();
      }
    } catch (IOException io) {
      System.err.println("===>[Parser] Unexpected IO error");
      io.printStackTrace();
      Thread.currentThread().interrupt();
      return;
    }
  }

  /** Read instructions from the headers. */
  void parseProps() {
    generated = new Script();
    currentCharacter = new HashSet<Character>();
    for (String currentLine : configSrc) {
      if (currentLine.startsWith("@")) {
        Character c = ConfigParser.identifyCharacter(currentLine.substring(1), verbose);
        generated.characters.add(c);
      } else if (currentLine.startsWith("#")) {
        ConfigParser.parseProp(currentLine.substring(1), generated.props, verbose);
      }
    }
  }

  /** Apply sublineformat. */
  void applySublineFormat() {}

  /** Read actual lines, sublines and annotations. */
  void readLine() {
    heldAnnos = new ArrayList<Annotation>();
    heldSublines = new ArrayList<Subline>();
    for (currentIndex = 0; currentIndex < contentSrc.size(); currentIndex++) {
      currentLine = contentSrc.get(currentIndex);
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
        if (s.startsWith(ss)
            // Ensure there's at least some separation between name and line.
            && !Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", "" + s.split(ss)[1].charAt(0))
            && checkAmbiguity(currentLine, ss)) {
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
    try {
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
        if (verbose) {
          System.out.println("^===" + t.toString() + ": " + TextUtils.getAnnoContent(currentLine));
        }
        return true;
      }
    } catch (FormatException e) {
      System.err.println("===>[Parser] Wrong Annotation format, skipping...");
      return false;
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
      if (verbose) {
        System.out.println("\t" + currentLine);
      }
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
      if (verbose) {
        System.out.println("V===" + a.type.toString() + ": " + a.content);
      }
    }
    for (Subline s : heldSublines) {
      s.characters = l.characters;
      l.addSublineBefore(s);
      if (verbose) {
        System.out.println("V\t" + currentLine);
      }
    }
    heldAnnos.clear();
    heldSublines.clear();
    generated.lines.add(l);
    if (verbose) {
      System.out.println(TextUtils.getCharaName(currentCharacter) + ": " + currentLine);
    }
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
      System.out.println("===>[Parser] In the following context: ");
      for (int i = currentIndex - 10; i < currentIndex + 10; i++) {
        if (i == currentIndex) {
          System.out.println("Here-> " + line);
          continue;
        }
        System.out.println("       " + contentSrc.get(i));
      }
      System.out.println("===>[Parser] Ambiguity detected at: " + line);
      System.out.print("===>[Parser] Should this line be " + name + "'s? (y/N):");
      String ss = DramaInteractive.scanner.nextLine();
      return ss.equalsIgnoreCase("y");
    }
    return true;
  }
}
