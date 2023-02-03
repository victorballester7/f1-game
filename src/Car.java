import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;

abstract public class Car {
  public static final double TOL = 0.001;
  public static int lengthOriginal;
  public static int widthOriginal;
  public static final double length = 5.5;
  public static final double width = 2;
  public int orderedPosition; // 1st, 2nd, ...
  public static double initialSteeringAngle;
  public double rotationAngle = Math.toRadians(0);
  public Track t;
  public Vector2D direction = new Vector2D(1, 0);
  public Vector2D position = new Vector2D();
  public Vector2D initialPosition = new Vector2D();
  public Vector2D velocity = new Vector2D();
  public BufferedImage img;
  public boolean isPlayer;
  public Color color;
  private static final double Cd = 0.7; // drag coefficient
  private static final double CrTrack = 30 * Cd; // rolling coefficient
  private static final double CrWeed = 50 * CrTrack; // rolling coefficient
  private static final double airDensity = 1.204; // air density
  private static final double area = 1.5; // cross sectional area
  private static final double m = 750; // mass of the car
  private static final double drift = 10; // drift constant
  public double appliedEngineForce = 0; // 0 or engineMAX
  public double engineMAX; // between 600 and 800
  public double appliedBrakeForce; // 0 or brakeMAX
  public double brakeMAX; // ~ 8000
  private boolean isDrifting;

  public Car(int orderedPosition, Track t, boolean isPlayer) {
    this.orderedPosition = orderedPosition;
    this.t = t;
    this.isPlayer = isPlayer;
    img = setImgCar();
    lengthOriginal = img.getWidth(null);
    widthOriginal = img.getHeight(null);
    position = setInitialPosition(t.startDirection, orderedPosition);
    initialPosition = position;
    setCarFeatures();
  }

  private BufferedImage setImgCar() {
    BufferedImage img = null;
    // BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    ;
    String fileName = getCarImagePath();
    try {
      img = ImageIO.read(new File(fileName));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return img;
  }

  public static Vector2D setInitialPosition(Vector2D startDirection, int orderedPosition) {
    double sepFromLine = length * 1.5;
    initialSteeringAngle = Vector2D.signedAngle(new Vector2D(0, -1), startDirection);
    double x, y;

    x = Track.finishLinePoint.x - sepFromLine - length / 2 - (orderedPosition - 1) * length;

    if (orderedPosition % 2 == 1) { // 1st, 3rd, 5th.. cars
      y = Track.finishLinePoint.y + Track.widthTrack / 4;
    } else {// 2nd, 4th, 6th.. cars
      y = Track.finishLinePoint.y + 3 * Track.widthTrack / 4;
    }
    return new Vector2D(x, y);
  }

  abstract String getCarImagePath();

  abstract void setCarFeatures();

  public double speed() {
    return velocity.norm();
  }

  public double rotationRate() { // function a * x / (b + x^2) because it goes to zero at infinity (no steering wheel is hard to move) and it is zero at the origin (because we can turn the car it it is stopped)
    // it is in degree
    double scale = 7.5;
    double displacement = 20;
    return scale * speed() / (displacement + speed() * speed());
  }

  public boolean isOnTrack() {
    return t.path.area.contains(position.x, position.y);
  }

  public void update() {
    // rotationAngle = Math.signum(direction.x) * Math.acos(direction.dotProduct(velocity) /( direction.norm() * speed())); // dot product

    // reset direction parameters.
    // direction.x = 0;
    // direction.y = -1;
    // direction = direction.rotate(rotationAngle);
    rotationAngle = Vector2D.signedAngle(t.startDirection, direction);

    // double dif = 0.05 * (speed() / 30.0);
    // double difAngle = Vector2D.signedAngle(velocity, direction);
    // if (!Double.isNaN(difAngle)) {
    // // velocity = velocity.rotate(difAngle / Math.toRadians(drift * rotationRate * speed()));
    // double r = Math.random() * 50;
    // // velocity.rotateZ(difAngle / (100 * (5 * dif)));
    // velocity = velocity.rotate(difAngle / ((50 + r) * (5 * dif)));
    // // isDrifting = false;
    // }
    // velocity = velocity.rotate(difAngle / Math.toRadians(drift * rotationRate * speed()));
    double r = Math.random() * 50;
    // velocity.rotateZ(difAngle / (100 * (5 * dif)));
    double angle = Vector2D.signedAngle(velocity, direction);
    if (!Double.isNaN(angle)) {
      velocity = velocity.rotate(angle / (1 + speed()));
      isDrifting = Math.abs(Math.toDegrees(angle)) > 10;
    }
    if (isDrifting) {
      appliedEngineForce *= 0.2;
      appliedBrakeForce *= 0.2;
    }
    Vector2D[] newData = rk4();
    position = newData[0];
    velocity = newData[1];

    if (isPlayer) {
      System.out.println("direction: " + direction + " norm: " + direction.norm());
      System.out.println("position: " + position + " norm: " + position.norm());
      System.out.println("velocity: " + velocity + " norm: " + speed() + " norm (km/h): " + 3.6 * speed());
      System.out.println("rotationRate: " + rotationRate());
      System.out.println("direction: " + direction + " norm: " + direction.norm());
      System.out.println("rotation: " + rotationAngle);
      System.out.println("engine force: " + engineForce() + " norm: " + engineForce().norm());
      System.out.println("braking force: " + brakeForce() + " norm: " + brakeForce().norm());
      System.out.println("drag force: " + dragForce() + " norm: " + dragForce().norm());
      System.out.println("friction force: " + rollingResistanceForce() + " norm: " + rollingResistanceForce().norm());
      System.out.println("isOnTrack: " + isOnTrack());
    }
    // direction.rotate(rotationAngle, position);
    // rotationAngle += Math.toRadians(20);
    // position.rotate(rotationAngle);
  }

  private Vector2D[] systemODEs(Vector2D[] X) {
    Vector2D rDot = X[1]; // X[1] velocity
    Vector2D vDot = Vector2D.sum(engineForce(), brakeForce(), dragForce(), rollingResistanceForce()).scalarProd(1 / m);

    return new Vector2D[] {rDot, vDot};
  }

  private Vector2D engineForce() {
    return direction.scalarProd(appliedEngineForce);
  }

  private Vector2D brakeForce() {
    if (speed() < TOL)
      return new Vector2D();
    return velocity.scalarProd(-appliedBrakeForce / speed());
  }

  private Vector2D dragForce() {
    return velocity.scalarProd(-0.5 * Cd * area * airDensity * speed());
  }

  private Vector2D rollingResistanceForce() {
    if (isOnTrack())
      return velocity.scalarProd(-CrTrack);
    else
      return velocity.scalarProd(-CrWeed);
  }

  private Vector2D[] rk4() { // Runge - Kutta 4
    double step = 0.03; // step in seconds
    // double step = 1. / Window.FRAMES; // step in seconds
    Vector2D[] initialPoints = new Vector2D[] {position, velocity};

    // Define the k's of the Runge - Kutta.
    Vector2D[] k1 = systemODEs(initialPoints);
    Vector2D[] k2 = systemODEs(Vector2D.sum(initialPoints, Vector2D.scalarProd(k1, step / 2)));
    Vector2D[] k3 = systemODEs(Vector2D.sum(initialPoints, Vector2D.scalarProd(k2, step / 2)));
    Vector2D[] k4 = systemODEs(Vector2D.sum(initialPoints, Vector2D.scalarProd(k3, step)));

    Vector2D[] finalPoints = Vector2D.sum(initialPoints, Vector2D.scalarProd(Vector2D.sum(k1, Vector2D.scalarProd(k2, 2), Vector2D.scalarProd(k3, 2), k4), step / 6));

    return finalPoints;
  }

  public void drawCar(Graphics2D g2, AffineTransform at) {
    double ratio = length / img.getHeight();

    AffineTransform at2 = (AffineTransform) at.clone();

    // 4. translate it to the center of the component
    at2.translate(position.x, position.y);

    // 3. do the actual rotation
    at2.rotate(rotationAngle + initialSteeringAngle);

    // 2. just a scale because this image is big
    at2.scale(ratio, ratio);

    // 1. translate the object so that you rotate it around the
    // center (easier :))
    at2.translate(-img.getWidth() / 2, -img.getHeight() / 2);

    g2.drawImage(img, at2, null);
  }
}

class McLaren extends Car {

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
  }
}

class Ferrari extends Car {

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
    color = new Color(141, 38, 27);
  }

}

class RedBull extends Car {

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
  }

}