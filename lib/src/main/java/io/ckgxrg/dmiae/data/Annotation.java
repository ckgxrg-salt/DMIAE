package io.ckgxrg.dmiae.data;

import java.io.Serializable;

/** Represents data structure of an Annotation. */
public class Annotation implements ILineAttached, Serializable {

  private static final long serialVersionUID = 1883834815399394973L;

  public Line parent;
  public AnnotationType type;
  public String content;

  /**
   * Creates an Annotation with the given content and prop.
   *
   * @param content The content
   * @param prop The Annotation type
   */
  public Annotation(String content, AnnotationType prop) {
    this.content = content;
    this.type = prop;
  }

  @Override
  public Line getParent() {
    return this.parent;
  }

  @Override
  public void setParent(Line parent) {
    this.parent = parent;
  }
}
