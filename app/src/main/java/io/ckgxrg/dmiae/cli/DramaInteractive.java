package io.ckgxrg.dmiae.cli;

import java.util.Scanner;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/** Main entry of the CLI implementation. */
@Command(
    name = "dmiae",
    mixinStandardHelpOptions = true,
    version = "alpha 0.2.0",
    description =
        "DraMa.InterActivE, a tool that helps format drama scripts and export them in different"
            + " formats.",
    subcommands = RawParser.class)
public class DramaInteractive implements Callable<Integer> {

  public static Scanner scanner;

  @Override
  public Integer call() throws Exception {
    CommandLine.usage(this, System.out);
    return 0;
  }

  /** The main method. (Do we really need a Javadoc for this checkstyle?) */
  public static void main(String[] args) {
    scanner = new Scanner(System.in);
    int exitcode = new CommandLine(new DramaInteractive()).execute(args);
    scanner.close();
    System.exit(exitcode);
  }
}
