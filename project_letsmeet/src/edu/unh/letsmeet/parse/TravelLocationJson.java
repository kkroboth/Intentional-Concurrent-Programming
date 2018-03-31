package edu.unh.letsmeet.parse;

import edu.unh.letsmeet.storage.TravelLocation;

public class TravelLocationJson {
  public int id;
  public String country;
  public String city;
  public double population;
  public float[] latlong;

  public TravelLocationJson(TravelLocation travelLocation) {
    this.id = travelLocation.getId();
    this.country = travelLocation.getCountry();
    this.city = travelLocation.getCity();
    this.population = travelLocation.getPopulation();
    this.latlong = travelLocation.getLatlong();
  }
}
