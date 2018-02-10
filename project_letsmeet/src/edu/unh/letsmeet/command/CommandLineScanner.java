package edu.unh.letsmeet.command;

import java.util.Scanner;

/**
 * Continues to read commands from System.in
 */
public class CommandLineScanner {
  private final Scanner scanner;

  public CommandLineScanner() {
    scanner = new Scanner(System.in);
  }

  /**
   * Will block and continue to read input from Scanner.
   * Can be interrupted.
   */
  public void parseInput() {
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      System.out.println(line);
    }
  }

}
