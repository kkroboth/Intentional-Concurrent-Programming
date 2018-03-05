package edu.unh.letsmeet.storage;

import icp.core.ICP;
import icp.core.Permissions;

/**
 * Immutable object containing travel location
 * id, names, spatial location.
 */
public class TravelLocation {
  private final int id;
  private final String country;
  private final String place;
  private final float[] latlong;


  public TravelLocation(int id, String country, String place, float[] latlong) {
    this.id = id;
    this.country = country;
    this.place = place;
    this.latlong = latlong;

    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  public int getId() {
    return id;
  }

  public String getCountry() {
    return country;
  }

  public String getPlace() {
    return place;
  }

  public float[] getLatlong() {
    return latlong;
  }
}
