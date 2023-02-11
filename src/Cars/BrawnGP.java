package Cars;

import java.awt.*;

import Circuit.Track;

public class BrawnGP extends Car {
  public BrawnGP(int orderedPosition, Track t, boolean isPlayer) {
    super(orderedPosition, t, isPlayer);
  }

  @Override
  public String getCarImagePath() {
    return "images/cars/carBrawnGP.png";
  }

  @Override
  public void setCarFeatures() {
    engineMAX = 8500;
    brakeMAX = 9000;
    color = new Color(136, 215, 33);
    color2 = new Color(223, 223, 223);
  }

  @Override
  public String toString() {
    return "BrawnGP";
  }
}