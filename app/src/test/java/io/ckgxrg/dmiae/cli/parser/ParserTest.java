package io.ckgxrg.dmiae.cli.parser;

import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** Tests methods provided by Parser. */
public class ParserTest {

  Parser subject;

  @Test
  @Disabled("uhhhhhh")
  public void overviewTest() {
    subject = new Parser(new File("src/test/resources/overview_normal.md"));
    Assertions.assertEquals(
        "#Name:Testy\n"
            + "#Author:           Whom?\n"
            + "#Credits:Ohhhh so this is a very"
            + " looooooooooooooooooooooooooooooooooooooooooooooooooong description which even will"
            + " be automatically wrapped to the next line by nvim.\n"
            + "@Parser,Subject",
        subject.configSrc);
  }

  @Test
  public void checkAmbiguityTest() {}
}
