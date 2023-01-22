import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.BorderFactory.*;
import java.io.*;

public class Game {
  public static final Color myColor = new Color(255, 74, 74);
  public static Image backImg = null;
  private static double widthCarView;
  public Image[] lightsImg = new Image[7];
  public static final int lightsImgHeight = 100;
  public static final int lightsImgWidth = lightsImgHeight / 4 * 9;
  public boolean lightsOut = false;
  public static final int lightsDelay = 1000; // in milliseconds
  public AffineTransform atWorldToScreen;
  public AffineTransform atWorldToSmallScreen;
  public AffineTransform atCarToScreen;
  public static final int NumCars = 6;
  public Track t;
  public Car[] cars = new Car[NumCars];
  public Car myCar;
  public boolean keyLeft = false;
  public boolean keyRight = false;
  public boolean keyUp = false;
  public boolean keyDown = false;

  public Game() {
    initialize();
  }

  private void initialize() {
    t = new OvalTrack(17, 300, 300, new Vector2D(1, 0));
    widthCarView = Track.widthTrack * 2;

    cars[0] = new McLaren(1, t, true);
    cars[1] = new Ferrari(2, t, false);
    cars[2] = new RedBull(3, t, false);
    cars[3] = new McLaren(4, t, false);
    cars[4] = new Ferrari(5, t, false);
    cars[5] = new RedBull(6, t, false);

    myCar = cars[0];

    try {
      // background
      backImg = ImageIO.read(new File("images/misc/grass.jpg"));
      backImg = backImg.getScaledInstance(Window.frame.getWidth(), Window.frame.getHeight(), Image.SCALE_DEFAULT);

      // lights
      lightsImg[0] = ImageIO.read(new File("images/lights/off.png"));
      for (int i = 1; i < 6; i++) {
        lightsImg[i] = ImageIO.read(new File("images/lights/red" + i + ".png"));
      }
      lightsImg[6] = ImageIO.read(new File("images/lights/green.png"));
      for (int i = 0; i < 7; i++) {
        lightsImg[i] = lightsImg[i].getScaledInstance(lightsImgWidth, lightsImgHeight, Image.SCALE_DEFAULT);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void update() {
    // System.out.println("position " + myCar.position.print());
    // System.out.println("position finish line " + Track.finishLinePoint.print());
    // System.out.println("height frame: " + Window.frame.getHeight());
    // if (viewButton.isPressed)
    worldToScreenTransform();
    worldToSmallScreenTransform();
    // else
    carToScreenTransform();
    if (lightsOut) {
      for (Car c : cars) {// Cars
        if (c.isPlayer) {
          if (keyLeft)
            c.direction = c.direction.rotate(Math.toRadians(-c.rotationRate()));

          if (keyRight)
            c.direction = c.direction.rotate(Math.toRadians(c.rotationRate()));

          if (keyUp)
            c.appliedEngineForce = c.engineMAX;
          else
            c.appliedEngineForce = 0;

          if (keyDown)
            c.appliedBrakeForce = c.brakeMAX;
          else
            c.appliedBrakeForce = 0;
        }
        c.update();
      }
    }
  }

  public void run() {
    while (true) {
      update();
      Window.mainPanel.repaint();
      sleep(1000 / Window.FPS);
    }
  }

  public static final void sleep(long time) {
    try {
      Thread.sleep(time); // in milliseconds
    } catch (InterruptedException e) {
      System.out.println(e);
    }
  }

  public void worldToScreenTransform() {
    atWorldToScreen = new AffineTransform();

    atWorldToScreen.translate(Window.frame.getWidth() / 2, Window.frame.getHeight() / 2);
    atWorldToScreen.scale(Window.frame.getWidth() / t.bounds.getWidth(), Window.frame.getHeight() / t.bounds.getHeight());
    atWorldToScreen.translate(-t.bounds.getCenterX(), -t.bounds.getCenterY());
  }

  public void worldToSmallScreenTransform() {
    atWorldToSmallScreen = new AffineTransform();

    atWorldToSmallScreen.translate(Window.smallCircuitPanel.getWidth() / 2, Window.smallCircuitPanel.getHeight() / 2);
    atWorldToSmallScreen.scale(Window.smallCircuitPanel.scale * Window.frame.getWidth() / t.bounds.getWidth(), Window.smallCircuitPanel.scale * Window.frame.getHeight() / t.bounds.getHeight());
    atWorldToSmallScreen.translate(-t.bounds.getCenterX(), -t.bounds.getCenterY());
  }

  public void carToScreenTransform() {
    // double angle = Vector2D.signedAngle(myCar.direction, t.startDirection);
    double ratio = Window.frame.getWidth() / widthCarView;
    atCarToScreen = new AffineTransform();

    atCarToScreen.translate(Window.frame.getWidth() / 2, Window.frame.getHeight() / 2);
    atCarToScreen.scale(ratio, ratio);
    atCarToScreen.rotate(-(myCar.rotationAngle + Car.initialSteeringAngle));
    atCarToScreen.translate(-myCar.position.x, -myCar.position.y);
  }

}
