package io.ckgxrg.dmiae.cli.parser;

/** Thrown when DMIAE encounters a line that is not properly formatted. */
public class FormatException extends Exception {

  /** Constructs such an exception. */
  public FormatException(String msg) {
    super(msg);
  }
}
