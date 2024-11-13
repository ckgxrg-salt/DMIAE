package io.ckgxrg.dmiae.cli.parser;

import java.util.Optional;

/** Runtime flags for the DMIAE Parser. */
public class Flags {

  // Script configuration
  public Optional<String> name;
  public Optional<String> author;
  public Optional<String> credits;

  // DMIAE runtime flags
  public Optional<String> sublineformat;

  /** Go away null pointers. */
  public Flags() {
    name = Optional.empty();
    author = Optional.empty();
    credits = Optional.empty();

    sublineformat = Optional.empty();
  }
}
