package edu.unh.letsmeet.api.extractors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import edu.unh.letsmeet.api.Extractor;
import icp.core.ICP;
import icp.core.Permissions;

import java.io.IOException;
import java.util.Iterator;

public class EventsExtractor extends JsonExtractor {
  private static final EventsExtractor INSTANCE = new EventsExtractor();

  public static Extractor get() {
    return INSTANCE;
  }

  {
    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  private EventsExtractor() {

  }

  @Override
  public JsonElement parseJson(JsonElement json) throws IOException {
    JsonArray payload = new JsonArray();

    JsonObject embedded = json.getAsJsonObject().get("_embedded").getAsJsonObject();
    JsonArray events = embedded.get("events").getAsJsonArray();

    Iterator<JsonElement> iterator = events.iterator();
    while (iterator.hasNext()) {
      JsonObject event = iterator.next().getAsJsonObject();
      JsonObject filteredEvent = new JsonObject();

      filteredEvent.add("name", event.get("name"));
      filteredEvent.add("url", event.get("url"));
      // Get first if any image
      JsonArray images = event.get("images").getAsJsonArray();
      if (images.size() > 1) {
        filteredEvent.add("image", images.get(0).getAsJsonObject());
      }
      filteredEvent.add("date", event.get("dates").getAsJsonObject().get("start"));
      payload.add(filteredEvent);
    }

    return payload;
  }
}
