package edu.unh.letsmeet.parse;

import edu.unh.letsmeet.storage.TravelLocation;

public class TravelLocationJson {
  public int id;
  public String country;
  public String place;
  public float[] location;

  public TravelLocationJson(TravelLocation travelLocation) {
    this.id = travelLocation.getId();
    this.country = travelLocation.getCountry();
    this.place = travelLocation.getPlace();
    this.location = travelLocation.getLatlong();
  }
}
