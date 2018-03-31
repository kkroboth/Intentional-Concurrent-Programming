package edu.unh.letsmeet.api.extractors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import edu.unh.letsmeet.api.Extractor;
import icp.core.ICP;
import icp.core.Permissions;

import java.io.IOException;
import java.util.Iterator;

public class WeatherHourlyExtractor extends JsonExtractor {
  private static final WeatherHourlyExtractor INSTANCE = new WeatherHourlyExtractor();

  public static Extractor get() {
    return INSTANCE;
  }

  {
    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  private WeatherHourlyExtractor() {

  }

  @Override
  public JsonElement parseJson(JsonElement json) throws IOException {
    JsonArray payload = new JsonArray();

    JsonObject res = ((JsonObject) json);
    JsonArray list = res.getAsJsonArray("list");

    Iterator<JsonElement> iterator = list.iterator();
    while (iterator.hasNext()) {
      JsonObject item = iterator.next().getAsJsonObject();
      JsonObject filteredItem = new JsonObject();

      filteredItem.add("date", item.get("dt"));
      JsonObject main = item.get("main").getAsJsonObject();
      filteredItem.add("temp", main.get("temp"));
      filteredItem.add("humidity", main.get("humidity"));
      filteredItem.add("weather", item.get("weather"));
      filteredItem.add("clouds", item.get("clouds").getAsJsonObject().get("all"));
      payload.add(filteredItem);
    }

    return payload;
  }
}
