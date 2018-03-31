package edu.unh.letsmeet.api;

import com.google.gson.JsonElement;

public class Source {
  private final String name;
  private final JsonElement json;

  Source(String name, JsonElement json) {
    this.name = name;
    this.json = json;
  }

  public String getName() {
    return name;
  }

  public JsonElement getJson() {
    return json;
  }
}
