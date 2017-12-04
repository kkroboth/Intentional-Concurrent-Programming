package applications.forkjoin.shared;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Static utilities for counting words.
 */
public final class WordCount {

  private WordCount() {
    // nope
  }

  /**
   * Count number of words in buffered reader.
   * Closes reader after.
   *
   * @param reader buffered reader to count words from
   * @param word   word to search
   * @return number of words in reader
   */
  public static int countWordsInFile(BufferedReader reader, String word) {
    try {
      String line;
      int count = 0;
      while ((line = reader.readLine()) != null) {
        String[] words = line.split("\\s+");
        for (String w : words) {
          if (w.equalsIgnoreCase(word)) count++;
        }
      }

      return count;
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
