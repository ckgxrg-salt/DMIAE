package io.ckgxrg.dmiae.data;

/** All possible Annotation types. */
public enum AnnotationType {
  CHARACTER,
  NOTE,
  LIGHTING,
  AUDIO,
  COMMAND;

  @Override
  public String toString() {
    switch (this) {
      case CHARACTER:
        return "Character";
      case NOTE:
        return "Note";
      case LIGHTING:
        return "Lighting";
      case AUDIO:
        return "Audio";
      case COMMAND:
        return "Command";
      default:
        return "Note";
    }
  }
}
