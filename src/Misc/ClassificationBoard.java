package Misc;

import java.awt.*;
import javax.swing.*;

import Cars.Car;
import Main.Window;

public class ClassificationBoard {
  public myLabel[] labels;
  public int numCars;
  public int x;
  public int y;
  public static final int widthLabels = 200;
  public static final int heightLabels = 30;
  // public static final double widthLabelsRelative = widthLabels * 1. / Window.widthDefaultScreen;
  // public static final double heightLabelsRelative = heightLabels * 1. / Window.heightDefaultScreen;

  public ClassificationBoard(Car[] cars, int x, int y) {
    numCars = cars.length;
    this.x = x;
    this.y = y;
    labels = new myLabel[numCars];
    for (int i = 0; i < numCars; i++) {
      labels[i] = new myLabel(" " + cars[i].orderedPosition + " - " + cars[i].toString(), x, y + heightLabels * i, widthLabels, heightLabels, cars[i].color, cars[i].color2);
    }
    // updateSize(f);
  }

  public void updateSize(JFrame f, Car[] cars) {
    // int x = (int) Math.round(f.getWidth() * xRelative);
    // int y = (int) Math.round(f.getHeight() * yRelative);
    // int width = (int) Math.round(f.getWidth() * widthLabelsRelative);
    // int height = (int) Math.round(f.getHeight() * heightLabelsRelative);
    for (int i = 0; i < numCars; i++) {
      // if (cars[i].hasFinished()) {
      // labels[i].setText(" " + cars[i].finalPosition + " - " + cars[i].toString());
      // labels[i].y = y + (cars[i].finalPosition - 1) * heightLabels;
      // } else {
      labels[i].setText(" " + cars[i].orderedPosition + " - " + cars[i].toString());
      labels[i].y = y + (cars[i].orderedPosition - 1) * heightLabels;
      // }
      labels[i].updateSize(f);
    }
  }

  public void hide() {
    for (myLabel label : labels)
      label.setVisible(false);
  }

  public void show() {
    for (myLabel label : labels)
      label.setVisible(true);
  }
}
