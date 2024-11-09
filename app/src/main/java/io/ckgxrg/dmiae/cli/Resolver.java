package io.ckgxrg.dmiae.cli;

import io.ckgxrg.dmiae.data.*;
import io.ckgxrg.dmiae.data.Character;
import io.ckgxrg.dmiae.util.TextUtils;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

/* The Drama.Interactive resolver.
 * Intended to be called as $dmiae resolve raw <file>.
 * Reads instructions and lines from raw DMIAE formatted text files. */
public class Resolver implements Runnable {

  FileReader f;
  BufferedReader br;

  Script sc;
  String currentLine;
  HashSet<Character> currentCharacter = new HashSet<Character>();

  public static Resolver INSTANCE;
  public Thread t;
  public Scanner in = new Scanner(System.in);

  public ArrayList<Subline> heldSublines;
  public ArrayList<Annotation> heldAnnos;

  public void begin(String file) {
    try {
      f = new FileReader(file);
      t = new Thread(this, "dmiae-resolver");
      System.out.println("===>[DMIAE] Evaluating file: " + file);
      sc = new Script();
      heldSublines = new ArrayList<Subline>();
      heldAnnos = new ArrayList<Annotation>();
      t.start();
    } catch (FileNotFoundException e) {
      System.out.println("===>[DMIAE] Unable to start Resolver: File " + file + " does not exist!");
      e.printStackTrace();
    }
  }

  // Read instructions from the headers
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
    p = true;
  }

  // Process annotations
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

  // Process sublines
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

  // Flags, pc counts prev sublines, ac counts after sublines, p indicates in prev state or after
  // state
  int pc = 0;
  int ac = 0;
  boolean p = true;

  boolean formattedReadSubline() {
    if (p) {
      if (pc < prevcount) {
        currentLine = TextUtils.rmColon(currentLine);
        identifyChara(currentLine);
        heldSublines.add(new Subline(currentLine));
        if (++pc >= prevcount) {
          pc = 0;
          p = !p;
        }
        return true;
      } else {
        pc = 0;
        p = !p;
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
          p = !p;
        }
        return true;
      } else {
        ac = 0;
        p = !p;
        return false;
      }
    }
  }

  // Finally it could be an actual line
  public void readMainLine() {
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

  // Read actual lines, sublines and annotations
  public void readLine() {
    // Skip blank lines
    if (currentLine.isBlank()) return;

    // Attempt to update the current Character
    identifyChara(currentLine);
    currentLine = TextUtils.rmSpaces(currentLine);
    if (currentLine.isBlank()) return;

    // Manually set main line detection
    if (currentLine.startsWith("::")) {
      currentLine = TextUtils.untilLetter(currentLine);
      readMainLine();
      return;
    }

    // Annotation and Subline detection
    if (readAnno()) return;
    if (readSubline()) return;

    // Apply SublineFormat
    if (sc.props.sublineformat != null && formattedReadSubline()) return;

    readMainLine();
  }

  // Attempt to match the known names of Characters to look for Character lines.
  public void identifyChara(String s) {
    // First check if this line has multi-character
    if (s.contains("/")) {
      identifyMultiChara(s);
      return;
    }

    // Try the declared characters
    for (Character c : sc.characters) {
      for (String ss : c.names) {
        if (s.startsWith(ss) && TextUtils.checkWeakDeclaration(currentLine, ss)) {
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

  public void identifyMultiChara(String s) {
    String[] names = s.split("/");
    HashSet<Character> chs = new HashSet<Character>();
    int expect = 1;
    String last = "";
    for (String d : names) {
      d = TextUtils.rmSpaces(d);
      for (Character c : sc.characters) {
        for (String n : c.names) {
          if (n.equals(d)) {
            if (chs.contains(c)) expect--;
            chs.add(c);
            break;
          }
        }
      }
      for (Character c : Character.fallbacks) {
        for (String n : c.names) {
          if (n.equals(d)) {
            if (chs.contains(c)) expect--;
            chs.add(c);
            break;
          }
        }
      }
      last = d;
      if (chs.size() < expect) break;
      expect++;
    }
    if (chs.isEmpty()) return;
    currentCharacter.clear();
    currentCharacter = chs;
    String[] tmp = currentLine.split(last);
    currentLine = tmp.length == 1 ? "" : tmp[1];
    currentLine = TextUtils.untilLetter(currentLine);
  }

  // The thread main method.
  @Override
  @Deprecated
  public void run() {
    br = new BufferedReader(f);
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
      f.close();
      in.close();
    } catch (IOException e) {
      System.out.println("===>[DMIAE] Unexpected error");
      e.printStackTrace();
    }
  }
}
