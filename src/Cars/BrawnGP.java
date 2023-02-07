package Cars;

import java.awt.*;

import Circuit.Track;

public class BrawnGP extends Car {
  public BrawnGP(int orderedPosition, Track t, boolean isPlayer) {
    super(orderedPosition, t, isPlayer);
  }

  @Override
  String getCarImagePath() {
    return "images/cars/carBrawnGP.png";
  }

  @Override
  void setCarFeatures() {
    engineMAX = 8500;
    brakeMAX = 9000;
    color = new Color(183, 255, 89);
    color2 = new Color(223, 223, 223);
  }
}