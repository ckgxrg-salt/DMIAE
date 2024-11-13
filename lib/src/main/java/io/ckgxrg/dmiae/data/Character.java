package io.ckgxrg.dmiae.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/** Data structure of a Character. */
public class Character {

  // Fallback Characters, if none of declared characters identified DMIAE will attempt these.
  public static Character Everyone =
      new Character(
          "Everyone",
          "EVERYONE",
          "All",
          "ALL",
          "Ensemble",
          "ENSEMBLE",
          "全体",
          "全体演员",
          "全体成员",
          "所有人");
  public static Character Chorus =
      new Character("Company", "COMPANY", "Chorus", "CHORUS", "合唱", "合唱团", "伴唱", "伴舞");
  public static HashSet<Character> fallbacks =
      new HashSet<Character>(Arrays.asList(Everyone, Chorus));

  public ArrayList<String> names;
  public String desc;

  /** Poor character with no name. */
  public Character() {
    names = new ArrayList<String>();
  }

  /** Construct a Character with the given names. */
  public Character(String... names) {
    this.names = new ArrayList<String>();
    for (String s : names) {
      this.names.add(s);
    }
    this.desc = "";
  }

  /** Adds a description to the Character. */
  public void addDesc(String desc) {
    this.desc = desc;
  }

  /** DMIAE will need one single name to call the Character. */
  public String getAmbiguousName() {
    return (String) names.toArray()[0];
  }
}
