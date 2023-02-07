package Cars;

import java.awt.*;

import Circuit.Track;

public class Ferrari extends Car {

  public Ferrari(int orderedPosition, Track t, boolean isPlayer) {
    super(orderedPosition, t, isPlayer);
  }

  @Override
  String getCarImagePath() {
    return "images/cars/carFerrari.png";
  }

  @Override
  void setCarFeatures() {
    engineMAX = 7500;
    brakeMAX = 11000;
    color = new Color(150, 40, 29);
    color2 = new Color(255, 255, 255);
  }

}