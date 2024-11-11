package io.ckgxrg.dmiae.util;

import io.ckgxrg.dmiae.data.AnnotationType;
import io.ckgxrg.dmiae.data.Character;
import java.util.HashSet;
import java.util.regex.Pattern;

/** This class provides some useful utilities with texts. */
public class TextUtils {

  /**
   * Remove leading spaces.
   *
   * @param s The String to work on
   * @return The processed String
   */
  public static String rmSpaces(String s) {
    String ss = "";
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) != ' ') {
        ss = s.substring(i);
        break;
      }
    }
    for (int j = ss.length() - 1; j >= 0; j--) {
      if (ss.charAt(j) != ' ') {
        return ss.substring(0, j + 1);
      }
    }
    return "";
  }

  /**
   * Remove character names.
   *
   * @param s The String to work on
   * @param name The character name to remove
   * @return The processed String
   */
  public static String rmName(String s, String name) {
    String[] sss = s.split(name);
    if (sss.length <= 1) {
      return "";
    }
    String ss = sss[1];
    return untilLetter(ss);
  }

  /**
   * Trim blank spaces before a String.
   *
   * @param s The String to work on
   * @return The processed String
   */
  public static String untilLetter(String s) {
    s = rmSpaces(s);
    for (int i = 0; i < s.length(); i++) {
      if (Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", "" + s.charAt(i))) {
        return s.substring(i);
      }
    }
    return "";
  }

  /**
   * Remove Subline leading colon.
   *
   * @param s The String to work on
   * @return The processed String
   */
  public static String rmColon(String s) {
    if (s.startsWith(":")) {
      return s.substring(1);
    }
    if (s.startsWith("<:") || s.startsWith(">:")) {
      return s.substring(2);
    }
    return s;
  }

  /**
   * Identifies the Annotation type.
   *
   * @param s The String to work on
   * @return The detected Annotation type
   */
  public static AnnotationType identifyAnnoType(String s) {
    String ss = s.split(":")[1];
    switch (ss) {
      case "LIGHTING", "LIGHT", "LT", "L":
        return AnnotationType.LIGHTING;
      case "AUDIO", "A", "MUSIC", "SOUND":
        return AnnotationType.AUDIO;
      case "CHARACTER":
        return AnnotationType.CHARACTER;
      default:
        return AnnotationType.NOTE;
    }
  }

  /**
   * Bad idea I allowed seeing Annotations with length 2 as note.
   *
   * @param s The String to work on
   * @return The content
   */
  public static String getAnnoContent(String s) {
    String[] ss = s.split(":");
    return ss.length <= 2 ? ss[1] : ss[2];
  }

  /**
   * Returns the names of characters.
   *
   * @param ch List of Characters to work on
   * @return A single String of their names
   */
  public static String getCharaName(HashSet<Character> ch) {
    StringBuilder sb = new StringBuilder();
    for (Character c : ch) {
      sb.append("@" + c.getAmbiguousName() + " / ");
    }
    sb.delete(sb.length() - 3, sb.length());
    return sb.toString();
  }
}