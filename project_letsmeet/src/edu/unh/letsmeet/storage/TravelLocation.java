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
  private final String city;
  private final float[] latlong;
  private final double population;
  private final String iso2;
  private final String iso3;

  public TravelLocation(int id, String city, float[] latlong, double population,
                        String country, String iso2, String iso3) {
    this.id = id;
    this.city = city;
    this.latlong = latlong;
    this.population = population;
    this.country = country;
    this.iso2 = iso2;
    this.iso3 = iso3;

    ICP.setPermission(this, Permissions.getFrozenPermission());
  }

  public int getId() {
    return id;
  }

  public String getCountry() {
    return country;
  }

  public String getCity() {
    return city;
  }

  public float[] getLatlong() {
    return latlong;
  }

  public double getPopulation() {
    return population;
  }

  public String getIso2() {
    return iso2;
  }

  public String getIso3() {
    return iso3;
  }
}
