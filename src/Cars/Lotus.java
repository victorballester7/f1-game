package Cars;

import java.awt.*;

import Circuit.Track;

public class Lotus extends Car {

  public Lotus(int orderedPosition, Track t, boolean isPlayer) {
    super(orderedPosition, t, isPlayer);
  }

  @Override
  public String getCarImagePath() {
    return "images/cars/carLotus.png";
  }

  @Override
  public void setCarFeatures() {
    engineMAX = 6000;
    brakeMAX = 15000;
    color = new Color(53, 53, 53);
    color2 = new Color(255, 221, 155);
  }

  @Override
  public String toString() {
    return "Lotus";
  }
}