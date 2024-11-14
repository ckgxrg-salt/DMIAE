package io.ckgxrg.dmiae.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

/** Represents all datas in a script. */
public class Script implements Serializable {

  private static final long serialVersionUID = 1883834815399394973L;

  public String name;
  public String author;
  public String credits;
  public ScriptProps props = new ScriptProps();

  public HashSet<Character> characters = new HashSet<Character>();
  public ArrayList<Line> lines = new ArrayList<Line>();

  /**
   * Adds a Character to the Script.
   *
   * @param ch The Character instance
   */
  public void addCharacter(Character ch) {
    this.characters.add(ch);
  }
}
