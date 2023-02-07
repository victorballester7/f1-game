package Cars;

import java.awt.*;

import Circuit.Track;

public class RedBull extends Car {

  public RedBull(int orderedPosition, Track t, boolean isPlayer) {
    super(orderedPosition, t, isPlayer);
  }

  @Override
  String getCarImagePath() {
    return "images/cars/carRedBull.png";
  }

  @Override
  void setCarFeatures() {
    engineMAX = 7000;
    brakeMAX = 12000;
    color = new Color(39, 39, 116);
    color2 = new Color(237, 243, 146);
  }
}
