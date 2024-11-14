package io.ckgxrg.dmiae.exceptions;

/** Thrown when a property, annotation or anything that does not comply the syntax of DMIAE. */
public class FormatException extends Exception {

  /** Constructor. */
  public FormatException(String message) {
    super(message);
  }
}
