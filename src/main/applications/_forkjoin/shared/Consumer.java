package applications._forkjoin.shared;

import applications._forkjoin.WordCount;

import java.util.List;

public final class Consumer {
  private final WordCount wordCount;

  public Consumer(WordCount wordCount) {
    this.wordCount = wordCount;
  }

  public List<WordCount.WordResults> getAllResults() {
    return wordCount.getResults();
  }
}
