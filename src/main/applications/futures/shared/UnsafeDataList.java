package applications.futures.shared;

import java.util.ArrayList;

public class UnsafeDataList {
  private ArrayList<Integer> results = new ArrayList<>();

  public void add(Integer i) {
    results.add(i);
  }

  public ArrayList<Integer> getList() {
    return results;
  }
}
