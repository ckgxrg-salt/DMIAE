package io.ckgxrg.dmiae.data;

import java.util.HashSet;

public class Subline extends Line implements ILineAttached {

  public Line parent;

  public Subline(String text, HashSet<Character> ch) {
    super(text, ch);
  }

  @Override
  public Line getParent() {
    return parent;
  }

  @Override
  public void setParent(Line parent) {
    this.parent = parent;
  }
}
