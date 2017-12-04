package applications._forkjoin.shared;

import applications._forkjoin.WordCount;

import java.util.List;

// Can't use do to Consumer Producer permission problem.
// In order for this to work, getAllResults() and addResult() would have to
// different permissions.
public final class SharedOperation {
  private WordCount wordCount;

  public SharedOperation(WordCount wordCount) {
    this.wordCount = wordCount;
  }

  public List<WordCount.WordResults> getAllResults() {
    return wordCount.getResults();
  }

  public void addResult(WordCount.WordResults results) {
    wordCount.addResult(results);
  }
}
