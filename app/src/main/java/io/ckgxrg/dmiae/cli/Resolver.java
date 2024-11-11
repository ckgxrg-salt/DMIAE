package io.ckgxrg.dmiae.cli;

import io.ckgxrg.dmiae.data.Annotation;
import io.ckgxrg.dmiae.data.AnnotationType;
import io.ckgxrg.dmiae.data.Character;
import io.ckgxrg.dmiae.data.Line;
import io.ckgxrg.dmiae.data.Script;
import io.ckgxrg.dmiae.data.Subline;
import io.ckgxrg.dmiae.util.TextUtils;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

/**
 * The Drama.Interactive resolver. Intended to be called as $dmiae resolve raw file. Reads
 * instructions and lines from raw DMIAE formatted text files.
 */
public class Resolver implements Runnable {

  FileReader src;
  BufferedReader br;

  Script sc;
  String currentLine;
  HashSet<Character> currentCharacter = new HashSet<Character>();

  public static Resolver INSTANCE;
  public Thread thread;
  public Scanner in = new Scanner(System.in);

  public ArrayList<Subline> heldSublines;
  public ArrayList<Annotation> heldAnnos;

  /**
   * Starts the Revolver.
   *
   * @param file Path to the script file
   */
  public void begin(String file) {
    try {
      src = new FileReader(file);
      thread = new Thread(this, "dmiae-resolver");
      System.out.println("===>[DMIAE] Evaluating file: " + file);
      sc = new Script();
      heldSublines = new ArrayList<Subline>();
      heldAnnos = new ArrayList<Annotation>();
      thread.start();
    } catch (FileNotFoundException e) {
      System.out.println("===>[DMIAE] Unable to start Resolver: File " + file + " does not exist!");
      e.printStackTrace();
    }
  }

  /** Read instructions from the headers. */
  void readInst() {
    if (currentLine.startsWith("@")) {
      Character c = InstructionHandler.handleCharacter(currentLine.substring(1));
      sc.characters.add(c);
    } else if (currentLine.startsWith("#")) {
      InstructionHandler.handleInst(currentLine.substring(1));
    }
  }

  // Flags for SublineFormat
  int prevcount;
  int aftercount;

  void initFlags() {
    String[] split = sc.props.sublineformat.split(",");
    prevcount = Integer.valueOf(split[0]);
    aftercount = Integer.valueOf(split[1]);
    prev = true;
  }

  /** Handle Annotations. */
  boolean readAnno() {
    if (currentLine.startsWith("@") || currentLine.startsWith(">@")) {
      heldAnnos.add(
          new Annotation(
              TextUtils.getAnnoContent(currentLine), TextUtils.identifyAnnoType(currentLine)));
      return true;
    } else if (currentLine.startsWith("<@")) {
      AnnotationType t = TextUtils.identifyAnnoType(currentLine);
      Annotation a = new Annotation(TextUtils.getAnnoContent(currentLine), t);
      a.parent = sc.lines.getLast();
      sc.lines.getLast().addAnnotationAfter(a);
      System.out.println("^===" + t.toString() + ": " + TextUtils.getAnnoContent(currentLine));
      return true;
    }
    return false;
  }

  /** Handle Sublines. */
  boolean readSubline() {
    if (currentLine.startsWith(">:")) {
      currentLine = TextUtils.rmColon(currentLine);
      identifyChara(currentLine);
      heldSublines.add(new Subline(currentLine));
      return true;
    } else if (currentLine.startsWith(":") || currentLine.startsWith("<:")) {
      currentLine = TextUtils.rmColon(currentLine);
      identifyChara(currentLine);
      Subline s = new Subline(currentLine);
      s.parent = sc.lines.getLast();
      sc.lines.getLast().addSublineAfter(s);
      System.out.println("\t" + currentLine);
      return true;
    }
    return false;
  }

  /**
   * Flags, pc counts prev sublines, ac counts after sublines, p indicates in prev state or after
   * state.
   */
  int pc = 0;

  int ac = 0;
  boolean prev = true;

  /** In case of a set SublineFormat, this will handle it. */
  boolean formattedReadSubline() {
    if (prev) {
      if (pc < prevcount) {
        currentLine = TextUtils.rmColon(currentLine);
        identifyChara(currentLine);
        heldSublines.add(new Subline(currentLine));
        if (++pc >= prevcount) {
          pc = 0;
          prev = !prev;
        }
        return true;
      } else {
        pc = 0;
        prev = !prev;
        return false;
      }
    } else {
      if (ac < aftercount) {
        currentLine = TextUtils.rmColon(currentLine);
        identifyChara(currentLine);
        Subline s = new Subline(currentLine);
        s.parent = sc.lines.getLast();
        sc.lines.getLast().addSublineAfter(s);
        // System.out.println("!pc, ac :" + pc + " " + ac);
        // System.out.println("!p :" + p);
        System.out.println("\t" + currentLine);
        if (++ac >= prevcount) {
          ac = 0;
          prev = !prev;
        }
        return true;
      } else {
        ac = 0;
        prev = !prev;
        return false;
      }
    }
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
    sc.lines.add(l);
    System.out.println(TextUtils.getCharaName(currentCharacter) + ": " + currentLine);
  }

  /** Read actual lines, sublines and annotations. */
  void readLine() {
    // Skip blank lines
    if (currentLine.isBlank()) {
      return;
    }

    // Attempt to update the current Character
    identifyChara(currentLine);
    currentLine = TextUtils.rmSpaces(currentLine);
    if (currentLine.isBlank()) {
      return;
    }

    // Manually set main line detection
    if (currentLine.startsWith("::")) {
      currentLine = TextUtils.untilLetter(currentLine);
      readMainLine();
      return;
    }

    // Annotation and Subline detection
    if (readAnno()) {
      return;
    }
    if (readSubline()) {
      return;
    }

    // Apply SublineFormat
    if (sc.props.sublineformat != null && formattedReadSubline()) {
      return;
    }

    readMainLine();
  }

  /** Attempt to match the known names of Characters to look for Character lines. */
  void identifyChara(String s) {
    // First check if this line has multi-character
    if (s.contains("/")) {
      identifyMultiChara(s);
      return;
    }

    // Try the declared characters
    for (Character c : sc.characters) {
      for (String ss : c.names) {
        if (s.startsWith(ss) && checkWeakDeclaration(currentLine, ss)) {
          currentLine = TextUtils.rmName(currentLine, ss);
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
          currentCharacter.clear();
          currentCharacter.add(c);
        }
      }
    }
  }

  /** The case if there are multiple characters in a single line. */
  void identifyMultiChara(String s) {
    String[] names = s.split("/");
    HashSet<Character> chs = new HashSet<Character>();
    int expect = 1;
    String last = "";
    for (String d : names) {
      d = TextUtils.rmSpaces(d);
      for (Character c : sc.characters) {
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
      if (chs.size() < expect) {
        break;
      }
      expect++;
    }
    if (chs.isEmpty()) {
      return;
    }
    currentCharacter.clear();
    currentCharacter = chs;
    String[] tmp = currentLine.split(last);
    currentLine = tmp.length == 1 ? "" : tmp[1];
    currentLine = TextUtils.untilLetter(currentLine);
  }

  /**
   * Intercepts the character identification if the Character's name is just separated with a single
   * space, which is ambiguous.
   *
   * @param line The current line
   * @param name The character name
   * @return Whether this should be intercepted
   */
  boolean checkWeakDeclaration(String line, String name) {
    if (line.split(name).length > 1 && line.split(name)[1].startsWith(" ")) {
      if (line.split(name)[1].isBlank()) {
        return true;
      }
      System.out.println("[DMIAE]===>Ambiguity detected at: " + line);
      System.out.print("[DMIAE]===>Should this line be " + name + "'s? (y/N):");
      String ss = Resolver.INSTANCE.in.nextLine();
      return ss.equalsIgnoreCase("y");
    }
    return true;
  }

  /** The thread main method. Do not invoke manually, use @see Resolver.start() instead. */
  @Override
  @Deprecated
  public void run() {
    br = new BufferedReader(src);
    try {
      INSTANCE = this;
      System.out.println("\r|==========RESOLVER====STARTS==========|");
      while ((currentLine = br.readLine()) != null) {
        // if(t.isInterrupted()) return;
        currentLine = TextUtils.rmSpaces(currentLine);
        if (currentLine.equals("#END")) {
          break;
        }
        readInst();
      }
      initFlags();
      System.out.println("\r|==========READ==LINES==BEGIN==========|");
      currentCharacter.add(Character.Everyone);
      while ((currentLine = br.readLine()) != null) {
        // if(t.isInterrupted()) return;
        currentLine = TextUtils.rmSpaces(currentLine);
        readLine();
      }
      System.out.println("|=========READ==LINES==COMPLETE=========|");
      br.close();
      src.close();
      in.close();
    } catch (IOException e) {
      System.out.println("===>[DMIAE] Unexpected error");
      e.printStackTrace();
    }
  }
}
