package Cars;

import java.awt.*;

import Circuit.Track;

public class Renault2016 extends Car {
  public Renault2016(int orderedPosition, Track t, boolean isPlayer) {
    super(orderedPosition, t, isPlayer);
  }

  @Override
  String getCarImagePath() {
    return "images/cars/carRenault2016.png";
  }

  @Override
  void setCarFeatures() {
    engineMAX = 6000;
    brakeMAX = 10000;
    color = new Color(204, 170, 9);
    color2 = new Color(0, 0, 0);
  }
}