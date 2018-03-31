package edu.unh.letsmeet.storage;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.opencsv.CSVIterator;
import com.opencsv.CSVReader;
import edu.unh.letsmeet.engine.Response;
import edu.unh.letsmeet.parse.TravelLocationJson;
import icp.core.ICP;
import icp.wrapper.ICPProxy;
import icp.core.Permissions;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <i>The</i> storage interface of lets meet application.
 */
public final class UniversalStorage {
  public static final Gson GSON = new Gson();

  // Immutable map -- after initialized
  private Map<Integer, TravelLocation> travelLocationMap;

  public UniversalStorage() {
    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

  public void parseAndSetTravelMap(Path csvFile) throws IOException {
    CSVReader reader = new CSVReader(Files.newBufferedReader(csvFile));
    CSVIterator iter = new CSVIterator(reader);
    Map<Integer, TravelLocation> travelLocationMap = new HashMap<>();

    // Skip header
    iter.next();
    while (iter.hasNext()) {
      String[] row = iter.next();
      assert row.length == 10;
      int id = Integer.parseInt(row[0]);
      String city = row[1];
      float lat = Float.parseFloat(row[3]);
      float lng = Float.parseFloat(row[4]);
      double population = Double.parseDouble(row[5]);
      String country = row[6];
      String iso2 = row[7];
      String iso3 = row[8];

      TravelLocation value = new TravelLocation(id, city, new float[]{lat, lng},
        population, country, iso2, iso3);
      travelLocationMap.put(Integer.parseInt(row[0]),
        value);
    }

    setTravelLocationMap(travelLocationMap);
  }

  /**
   * After setting map, the map's permission will be frozen.
   */
  public void setTravelLocationMap(Map<Integer, TravelLocation> map) {
    if (travelLocationMap != null) throw new IllegalStateException("Travel Location Map already set");
    Map<Integer, TravelLocation> immutableMap = Collections.unmodifiableMap(map);

    // Probably not needed
    this.travelLocationMap = ICPProxy.newFrozenInstance(Map.class, immutableMap);
  }

  public Map<Integer, TravelLocation> getTravelLocationMap() {
    return travelLocationMap;
  }

  public TravelLocation getTravelLocation(int id) {
    return travelLocationMap.get(id);
  }

  /**
   * Writes all data to stream and closes it.
   * <p>
   * Used when response is transfer chunked.
   */
  public void writeAllTravelLocations(OutputStream out) throws IOException {
    JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
    writer.setIndent(" ");
    writer.beginArray();
    for (TravelLocation travelLocation : travelLocationMap.values()) {
      GSON.toJson(new TravelLocationJson(travelLocation), TravelLocationJson.class, writer);
    }
    writer.endArray();
    writer.close();
  }

  /**
   * Sets response body and content type
   */
  public void writeAllTravelLocations(Response.Builder builder) throws IOException {
    Collection<TravelLocation> values = travelLocationMap.values();
    String jsonString = GSON.toJson(values);
    builder.json(jsonString);
  }
}
