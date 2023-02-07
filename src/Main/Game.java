package Main;

import java.awt.*;
import java.awt.geom.*;

import javax.imageio.ImageIO;
import Cars.*;

import Circuit.*;
import Misc.Vector2D;

import java.io.*;

public class Game {
  public static final Color myColor = new Color(255, 74, 74);
  public static final String fontName = "Arial";
  public static final int fontStyle = Font.BOLD;
  public static final int fontSize = 20;
  public static final int iconSize = 30; // assuming they are square
  public static Image backImg = null;
  private static double widthCarView;
  public Image[] lightsImg = new Image[7];
  public static final int lightsImgHeight = 100;
  public static final int lightsImgWidth = lightsImgHeight / 4 * 9;
  public boolean lightsOut = false;
  // public static final int lightsDelay = 1000; // in milliseconds
  public static final int lightsDelay = 100; // in milliseconds
  public AffineTransform atWorldToScreen;
  public AffineTransform atWorldToSmallScreen;
  public AffineTransform atCarToScreen;
  public static final int numCars = 9;
  public int currentCarView = 0;
  public Track t;
  public Car[] cars = new Car[numCars];
  public boolean keyLeft = false;
  public boolean keyRight = false;
  public boolean keyUp = false;
  public boolean keyDown = false;

  public Game() {
    initialize();
  }

  private void initialize() {
    t = new Indianapolis(17, 300, 300, new Vector2D(1, 0));
    // widthCarView = Track.widthTrack * 2;
    widthCarView = t.widthTrack * 10;

    cars[0] = new McLaren(1, t, true);
    cars[1] = new Ferrari(2, t, false);
    cars[2] = new RedBull(3, t, false);
    cars[3] = new BrawnGP(4, t, false);
    cars[4] = new Lotus(5, t, false);
    cars[5] = new Renault2005(6, t, false);
    cars[6] = new Renault2009(7, t, false);
    cars[7] = new Renault2016(8, t, false);
    cars[8] = new Williams(9, t, false);

    // Curve c = (Curve) t.pathsBestTrajectory[1];
    // Vector2D v = new Vector2D(1400, 500);
    // double t0 = c.getClosestPoint(v);
    // System.out.println(c.startPoint + "" + c.controlPoint1 + "" + c.controlPoint2
    // + "" + c.endPoint);
    // System.out.println(c.points[0] + "" + c.points[1] + "" + c.points[2] + "" +
    // c.points[3]);
    // System.out.println(t0 + " and " + c.bezier(t0));

    // for (double s = 0; s <= 1; s += 0.02) {
    // System.out.println("derivative at " + s + ": " + c.tangent(s));
    // }

    // for (int i = 0; i < numCars; i++) {
    // for (int j = 0; j < numCars; j++)
    // System.out.println("Car " + i + " is behind car " + j + ":" +
    // cars[i].isBehind(cars[j]));
    // }

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
    worldToScreenTransform();
    worldToSmallScreenTransform();
    carToScreenTransform();

    if (lightsOut) {
      for (int i = 0; i < numCars; i++) {// Cars
        Car c = cars[i];
        if (c.isPlayer) {
          System.out.println("Im here");
          if (keyLeft)
            c.direction = c.direction.rotate(Math.toRadians(c.rotationRate()));

          if (keyRight)
            c.direction = c.direction.rotate(Math.toRadians(-c.rotationRate()));

          if (keyUp)
            c.appliedEngineForce = c.engineMAX;
          else
            c.appliedEngineForce = 0;

          if (keyDown)
            c.appliedBrakeForce = c.brakeMAX;
          else
            c.appliedBrakeForce = 0;
        }
        // ordered positions updating.
        c.update();
        c.orderedPosition = 1;
        for (int j = 0; j < i; j++) {
          if (c.orderedPosition <= cars[j].orderedPosition) {
            if (c.isBehind(cars[j]))
              c.orderedPosition = cars[j].orderedPosition + 1;
            else
              cars[j].orderedPosition++;
          }
        }
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

  public AffineTransform symmetryX() {
    AffineTransform atSymmetry = new AffineTransform();
    atSymmetry.setTransform(1, 0, 0, -1, 0, 0); // symmetry
    return atSymmetry;
  }

  public void worldToScreenTransform() {
    atWorldToScreen = new AffineTransform();

    atWorldToScreen.translate(Window.frame.getWidth() / 2, Window.frame.getHeight() / 2);
    atWorldToScreen.scale(Window.frame.getWidth() / t.bounds.getWidth(), Window.frame.getHeight() / t.bounds.getHeight());
    atWorldToScreen.concatenate(symmetryX()); // symmetry (because the graphics are printed with inverted y-axis)
    atWorldToScreen.translate(-t.bounds.getCenterX(), -t.bounds.getCenterY());
  }

  public void worldToSmallScreenTransform() {
    atWorldToSmallScreen = new AffineTransform();

    atWorldToSmallScreen.translate(Window.smallCircuitPanel.getWidth() / 2, Window.smallCircuitPanel.getHeight() / 2);
    atWorldToSmallScreen.scale(Window.smallCircuitPanel.scale * Window.frame.getWidth() / t.bounds.getWidth(), Window.smallCircuitPanel.scale * Window.frame.getHeight() / t.bounds.getHeight());
    atWorldToSmallScreen.concatenate(symmetryX()); // symmetry
    atWorldToSmallScreen.translate(-t.bounds.getCenterX(), -t.bounds.getCenterY());
  }

  public void carToScreenTransform() {
    double ratio = Window.frame.getWidth() / widthCarView;
    atCarToScreen = new AffineTransform();

    atCarToScreen.translate(Window.frame.getWidth() / 2, Window.frame.getHeight() / 2);
    atCarToScreen.scale(ratio, ratio);

    Path p = t.paths[cars[currentCarView].currentPathIndex];
    double t0 = p.getClosestPoint(cars[currentCarView].position);
    double angle = Vector2D.signedAngle(t.startDirection, p.tangent(t0));
    atCarToScreen.rotate(angle - Car.initialSteeringAngle);
    atCarToScreen.concatenate(symmetryX()); // symmetry
    atCarToScreen.translate(-p.position(t0).x, -p.position(t0).y);
  }

}
