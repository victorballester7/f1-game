package Cars;

import java.awt.*;

import Circuit.Track;

public class Williams extends Car {
  public Williams(int orderedPosition, Track t, boolean isPlayer) {
    super(orderedPosition, t, isPlayer);
  }

  @Override
  String getCarImagePath() {
    return "images/cars/carWilliams.png";
  }

  @Override
  void setCarFeatures() {
    engineMAX = 9000;
    brakeMAX = 8000;
    color = new Color(220, 220, 220);
    color2 = new Color(18, 18, 137);
  }
}