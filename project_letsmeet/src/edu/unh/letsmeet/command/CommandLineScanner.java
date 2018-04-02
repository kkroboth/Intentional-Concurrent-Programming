package edu.unh.letsmeet.command;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Continues to read commands from System.in
 */
public class CommandLineScanner {
  private final Scanner scanner;

  public static Builder build() {
    return new Builder();
  }

  private CommandLineScanner(Builder builder) {
    this.scanner = new Scanner(builder.in);
  }

  /**
   * Will block and continue to read input from Scanner.
   * Can be interrupted.
   */
  public void start() {
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      System.out.println(line);
    }
  }

  public static class Builder {
    InputStream in;

    private Builder() {

    }
  }

  private static class Command {
    private final String name;
    private final Runnable execute;

    Command(String name, Runnable execute) {
      this.name = name;
      this.execute = execute;
    }
  }

}
