package Cars;

import java.awt.*;

import Circuit.Track;

public class Williams extends Car {
  public Williams(int orderedPosition, Track t, boolean isPlayer) {
    super(orderedPosition, t, isPlayer);
  }

  @Override
  public String getCarImagePath() {
    return "images/cars/carWilliams.png";
  }

  @Override
  public void setCarFeatures() {
    engineMAX = 9000;
    brakeMAX = 6000;
    color = new Color(220, 220, 220);
    color2 = new Color(18, 18, 137);
  }

  @Override
  public String toString() {
    return "Williams";
  }
}