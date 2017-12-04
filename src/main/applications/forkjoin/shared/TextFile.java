package applications.forkjoin.shared;

import icp.core.ICP;
import icp.core.Permissions;

import java.io.BufferedReader;
import java.io.InputStreamReader;

// Text file in resources folder
public class TextFile {
  public final String name;
  public final String word;

  public TextFile(String name, String word) {
    this.name = name;
    this.word = word;
    // TODO: Transfer permission?
    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  public BufferedReader open() {
    // TODO: Should we check if file is opened twice?
    return new BufferedReader(new InputStreamReader(getClass().getClassLoader()
      .getResourceAsStream(name)));
  }

}

