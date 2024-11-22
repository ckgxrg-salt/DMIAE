package io.ckgxrg.dmiae.data;

import java.io.Serializable;
import java.util.Optional;

/** Properties for a Script. */
public class ScriptProps implements Serializable {

  private static final long serialVersionUID = 1883834815399394973L;

  // Defaults
  public static final String DEFAULT_NAME = "";
  public static final String DEFAULT_AUTHOR = "";
  public static final String DEFAULT_CREDITS = "";

  // Script configuration
  public Optional<String> name;
  public Optional<String> author;
  public Optional<String> credits;

  /** Nobody likes you NullPointerException. */
  public ScriptProps() {
    name = Optional.empty();
    author = Optional.empty();
    credits = Optional.empty();
  }
}
