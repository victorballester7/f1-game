package Cars;

import java.awt.*;

import Circuit.Track;

public class Renault2009 extends Car {
  public Renault2009(int orderedPosition, Track t, boolean isPlayer) {
    super(orderedPosition, t, isPlayer);
  }

  @Override
  String getCarImagePath() {
    return "images/cars/carRenault2009.png";
  }

  @Override
  void setCarFeatures() {
    engineMAX = 6500;
    brakeMAX = 10500;
    color = new Color(228, 107, 10);
    color2 = new Color(237, 198, 0);
  }
}