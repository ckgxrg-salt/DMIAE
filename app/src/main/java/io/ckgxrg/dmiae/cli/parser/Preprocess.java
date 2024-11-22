package io.ckgxrg.dmiae.cli.parser;

import java.util.ArrayList;

/** Some private methods for pre-processing the plain text. */
public class Preprocess {
  /** Generate subline markers defined by #sublineFormat. */
  static ArrayList<String> applySublineMarker(String sublineformat, ArrayList<String> content) {
    String[] split = sublineformat.split(",");
    if (split.length != 2) {
      System.err.println("===>[Parser] Value of #sublineFormat is invalid, skipping...");
      return content;
    }
    try {
      int prevCount = Integer.valueOf(split[0].strip());
      int afterCount = Integer.valueOf(split[1].strip());
      int p = 0;
      int a = 0;
      boolean prev = true;
      ArrayList<String> overwrite = new ArrayList<String>();
      for (String currentLine : content) {
        if (prev && p < prevCount) {
          overwrite.add(">:" + currentLine);
          p++;
        } else if (!prev && a < afterCount) {
          overwrite.add("<:" + currentLine);
        } else if (p == prevCount || a == afterCount) {
          prev = !prev;
          overwrite.add(currentLine);
        }
      }
      return overwrite;
    } catch (NumberFormatException e) {
      System.err.println("===>[Parser] Value of #sublineFormat is invalid, disabling...");
      e.printStackTrace();
      return content;
    } catch (ArrayIndexOutOfBoundsException e) {
      System.err.println("===>[Parser] Value of #sublineFormat is invalid, disabling...");
      e.printStackTrace();
      return content;
    }
  }
}
