package Cars;

import java.awt.*;

import Circuit.Track;

public class McLaren extends Car {

  public McLaren(int orderedPosition, Track t, boolean isPlayer) {
    super(orderedPosition, t, isPlayer);
  }

  @Override
  String getCarImagePath() {
    return "images/cars/carMcLaren.png";
  }

  @Override
  void setCarFeatures() {
    engineMAX = 8000;
    brakeMAX = 10000;
    color = new Color(196, 210, 219);
    color2 = new Color(255, 23, 42);
  }
}
