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

  public Character() {
    names = new ArrayList<String>();
  }

  public Character(String... names) {
    this.names = new ArrayList<String>();
    Arrays.sort(names, (String a, String b) -> Integer.compare(a.length(), b.length()));
    for (String s : names) {
      this.names.add(s);
    }
    this.desc = "";
  }

  public void addDesc(String desc) {
    this.desc = desc;
  }

  public String getAmbiguousName() {
    return (String) names.toArray()[0];
  }
}
