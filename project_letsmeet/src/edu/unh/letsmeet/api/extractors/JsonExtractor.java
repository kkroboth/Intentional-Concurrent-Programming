package edu.unh.letsmeet.api.extractors;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import edu.unh.letsmeet.api.Extractor;
import icp.core.ICP;
import icp.core.Permissions;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class JsonExtractor implements Extractor {

  public static JsonExtractor identity() {
    return new JsonExtractor() {

      {
        ICP.setPermission(this, Permissions.getFrozenPermission());
      }

      @Override
      public JsonElement parseJson(JsonElement json) throws IOException {
        return json;
      }
    };
  }

  @Override
  public JsonElement parseContent(InputStream content) throws IOException {
    return parseJson(new JsonParser().parse(new InputStreamReader(content)));
  }

  public abstract JsonElement parseJson(JsonElement json) throws IOException;
}
