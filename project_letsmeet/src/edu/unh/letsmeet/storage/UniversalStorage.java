package edu.unh.letsmeet.storage;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.opencsv.CSVIterator;
import com.opencsv.CSVReader;
import edu.unh.letsmeet.parse.TravelLocationJson;
import icp.core.ICP;
import icp.core.Permissions;
import icp.wrapper.ICPProxy;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <i>The</i> storage interface of lets meet application.
 */
public final class UniversalStorage {
  // Immutable map -- after initialized
  private Map<Integer, TravelLocation> travelLocationMap;

  public UniversalStorage() {
    ICP.setPermission(this, Permissions.getThreadSafePermission());
  }

  public void parseAndSetTravelMap(Path csvFile) throws IOException {
    CSVReader reader = new CSVReader(Files.newBufferedReader(csvFile));
    CSVIterator iter = new CSVIterator(reader);
    Map<Integer, TravelLocation> travelLocationMap = new HashMap<>();
    while (iter.hasNext()) {
      String[] row = iter.next();
      assert row.length == 5;
      int id = Integer.parseInt(row[0]);
      String place = row[1];
      String country = row[2];
      float lat = Float.parseFloat(row[3]);
      float lng = Float.parseFloat(row[4]);
      travelLocationMap.put(Integer.parseInt(row[0]),
        new TravelLocation(id, country, place, new float[]{lat, lng}));
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
    this.travelLocationMap = ICPProxy.newInstance(Map.class, immutableMap,
      (proxyTarget, realTarget) -> ICP.setPermission(proxyTarget, Permissions.getFrozenPermission()));
  }

  public Map<Integer, TravelLocation> getTravelLocationMap() {
    return travelLocationMap;
  }

  public void writeAllTravelLocations(OutputStream out) throws IOException {
    Gson gson = new Gson();
    JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
    writer.setIndent(" ");
    writer.beginArray();
    for (TravelLocation travelLocation : travelLocationMap.values()) {
      gson.toJson(new TravelLocationJson(travelLocation), TravelLocationJson.class, writer);
    }
    writer.endArray();
    writer.close();
  }
}
