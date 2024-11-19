package io.ckgxrg.dmiae.cli;

import io.ckgxrg.dmiae.cli.parser.Parser;
import io.ckgxrg.dmiae.data.Script;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** Subcommand called when asked to parse an original text file to DMIAE raw file. */
@Command(name = "parse", description = "Parse a text file to DMIAE raw format.")
public class RawParser implements Callable<Integer> {

  @Parameters(index = "0", description = "Path to the file to be parsed.")
  private File file;

  @Option(
      names = {"-o", "--output"},
      description = "Name of the generated DMIAE raw file.")
  private String outPath;

  @Option(
      names = {"-v", "--verbose"},
      description = "Let the parser spit out tons of information.")
  private boolean verbose = false;

  @Override
  public Integer call() {
    try {
      ExecutorService es = Executors.newFixedThreadPool(1);
      Parser parser = new Parser(file, verbose);
      Future<Optional<Script>> future = es.submit(parser);
      Optional<Script> generated;
      if ((generated = future.get()).isPresent()) {
        // If user didn't pass outPath as argument, ask them now.
        if (outPath == null) {
          System.out.print("Name for the generated file: ");
          outPath = DramaInteractive.scanner.nextLine();
        }

        ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(outPath));
        objOut.writeObject(generated.get());
        System.out.println("===>[DMIAE] Wrote generated DMIAE file to " + outPath);
        objOut.close();
      } else {
        System.err.println("===>[DMIAE] Parser failed");
        return 1;
      }
    } catch (IOException e) {
      System.err.println("===>[DMIAE] Unable to save the file");
      e.printStackTrace();
      return 1;
    } catch (InterruptedException e) {
      System.err.println("===>[DMIAE] Interrupted");
      e.printStackTrace();
      return 1;
    } catch (ExecutionException e) {
      System.err.println("===>[DMIAE] Parser execution returned error");
      e.printStackTrace();
      return 1;
    }
    return 0;
  }
}
