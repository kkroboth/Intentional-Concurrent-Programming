package edu.unh.letsmeet.api.extractors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import edu.unh.letsmeet.api.Extractor;
import icp.core.ICP;
import icp.core.Permissions;

import java.io.IOException;
import java.util.Iterator;

public class RestaurantsExtractor extends JsonExtractor {
  private static final RestaurantsExtractor INSTANCE = new RestaurantsExtractor();

  public static Extractor get() {
    return INSTANCE;
  }

  {
    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  private RestaurantsExtractor() {

  }

  @Override
  public JsonElement parseJson(JsonElement json) throws IOException {
    JsonArray payload = new JsonArray();

    JsonArray restaurants = json.getAsJsonObject().getAsJsonArray("nearby_restaurants");
    Iterator<JsonElement> iterator = restaurants.iterator();
    while (iterator.hasNext()) {
      JsonObject item = iterator.next().getAsJsonObject().get("restaurant").getAsJsonObject();
      JsonObject filteredItem = new JsonObject();

      filteredItem.add("name", item.get("name"));
      filteredItem.add("url", item.get("url"));
      String image = item.get("featured_image").getAsString();
      if (image.isEmpty()) image = "/static/img/restaurant-no-image.jpg";
      filteredItem.addProperty("image", image);
      filteredItem.add("location", item.get("location"));
      filteredItem.add("rating", item.get("user_rating"));

      payload.add(filteredItem);
    }

    return payload;
  }
}
