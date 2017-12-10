package applications.forkjoin.shared;

import java.io.BufferedReader;
import java.io.InputStreamReader;

// Text file in resources folder
public class TextFile implements Runnable {
  public final String name;
  public final String word;

  // mutable field
  private int count;

  public TextFile(String name, String word) {
    this.count = -1;
    this.name = name;
    this.word = word;
  }

  public BufferedReader open() {
    // TODO: Should we check if file is opened twice?
    return new BufferedReader(new InputStreamReader(getClass().getClassLoader()
      .getResourceAsStream(name)));
  }

  public int getCount() {
    assert count != -1; // someone should of set if first
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  @Override
  public void run() {
    setCount(WordCount.countWordsInFile(open(), word));
  }
}

