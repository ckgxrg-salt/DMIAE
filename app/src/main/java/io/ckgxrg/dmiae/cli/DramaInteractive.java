package io.ckgxrg.dmiae.cli;

import io.ckgxrg.dmiae.cli.parser.Parser;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/** Main entry of the CLI implementation. */
@Command(
    name = "dmiae",
    mixinStandardHelpOptions = true,
    version = "alpha 0.1.0",
    description =
        "DraMa.InterActivE, a tool that helps to format drama scripts and export them in different"
            + " formats.")
public class DramaInteractive implements Callable<Integer> {

  @Parameters(index = "0", description = "Path to the file to be interpreted.")
  private String path;

  @Override
  public Integer call() throws Exception {
    new Parser().begin(path);
    return 0;
  }

  /** The main method. (Do we really need a Javadoc for this checkstyle?) */
  public static void main(String[] args) {
    new Parser().begin("/home/ckgxrg/script.md");
    // int exitcode = new CommandLine(new DramaInteractive()).execute(args);
    // System.exit(exitcode);
  }
}
