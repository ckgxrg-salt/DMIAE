package io.ckgxrg.dmiae.cli;

/** Main entry of the CLI implementation. */
public class DramaInteractive {

  /** The main method. (Do we really need a Javadoc for this checkstyle?) */
  public static void main(String[] args) {
    new Resolver().begin("/home/ckgxrg/eclipse-workspace/DMIAE/hami.txt");
  }
}
