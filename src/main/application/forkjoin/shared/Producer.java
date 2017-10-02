package application.forkjoin.shared;

import application.forkjoin.WordCount;

public final class Producer {
  private final WordCount wordCount;

  public Producer(WordCount wordCount) {
    this.wordCount = wordCount;
  }

  public void addResult(WordCount.WordResults job) {
    this.wordCount.addResult(job);
  }
}
