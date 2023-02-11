package Cars;

import java.awt.*;

import Circuit.Track;

public class Renault2005 extends Car {
  public Renault2005(int orderedPosition, Track t, boolean isPlayer) {
    super(orderedPosition, t, isPlayer);
  }

  @Override
  public String getCarImagePath() {
    return "images/cars/carRenault2005.png";
  }

  @Override
  public void setCarFeatures() {
    engineMAX = 7000;
    brakeMAX = 10500;
    color = new Color(58, 147, 209);
    color2 = new Color(237, 198, 0);
  }

  @Override
  public String toString() {
    return "Renault2005";
  }
}
