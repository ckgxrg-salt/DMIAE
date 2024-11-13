package io.ckgxrg.dmiae.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/** Represents a line in the scripts. */
public class Line {

  public String text;
  public Collection<Character> characters;
  ArrayList<Annotation> annobefore = new ArrayList<Annotation>();
  ArrayList<Annotation> annoafter = new ArrayList<Annotation>();
  ArrayList<Subline> subbefore = new ArrayList<Subline>();
  ArrayList<Subline> subafter = new ArrayList<Subline>();

  /**
   * Generates a line with the given character(s).
   *
   * @param text The text of the line
   * @param ch List of Characters of this line
   */
  public Line(String text, Collection<Character> ch) {
    this.text = text;
    this.characters = ch;
  }

  /**
   * Generates a line with the given character.
   *
   * @param text The text of the line
   * @param ch The character of this line
   */
  public Line(String text, Character ch) {
    this.text = text;
    this.characters = new HashSet<Character>();
    this.characters.add(ch);
  }

  /**
   * Adds an Annotation after the line.
   *
   * @param a The annotation to be added
   */
  public void addAnnotationAfter(Annotation a) {
    annoafter.add(a);
  }

  /**
   * Adds an Annotation before the line.
   *
   * @param a The annotation to be added
   */
  public void addAnnotationBefore(Annotation a) {
    annobefore.add(a);
  }

  /**
   * Adds an Subline after the line.
   *
   * @param sub The subline to be added
   */
  public void addSublineAfter(Subline sub) {
    subafter.add(sub);
  }

  /**
   * Adds an Subline after the line.
   *
   * @param sub the Subline to be added
   */
  public void addSublineBefore(Subline sub) {
    subbefore.add(sub);
  }
}
