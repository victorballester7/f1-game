import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;

abstract public class Car {
  public static final double TOL = 0.001;
  public static final int WIDTH_ORIGINAL = 38;
  public static final int HEIGHT_ORIGINAL = 2 * WIDTH_ORIGINAL;
  public static final int WIDTH = 10;
  public static final int HEIGHT = 2 * WIDTH;
  public int orderedPosition; // 1st, 2nd, ...
  private double initialSteeringAngle;
  public double rotationAngle = Math.toRadians(0);
  // public double slipRatio;
  public Track t;
  public Vector2D direction = new Vector2D(1, 0);
  public Vector2D position = new Vector2D();
  public Vector2D initialPosition = new Vector2D();
  public Vector2D velocity = new Vector2D();
  // public Vector2D acceleration = new Vector2D();
  // public double angularVelocityRearWheels = 0;
  // public double angularVelocityCar = 0;
  public BufferedImage img;
  public boolean isPlayer;
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
    img = setCar();
    setInitialPosition(t.startDirection);
    setCarFeatures();
  }

  private BufferedImage setCar() {
    BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
    ;
    String fileName = getCarImagePath();
    try {
      img = ImageIO.read(new File(fileName));
      // img = (BufferedImage) img.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);
    } catch (IOException e) {
      img = null;
      // e.printStackTrace();
    }
    return img;
  }

  public void setInitialPosition(Vector2D startDirection) {
    int sepFromLine = 10;
    initialSteeringAngle = Vector2D.signedAngle(new Vector2D(0, -1), startDirection);
    // rotationAngle = Vector2D.signedAngle(new Vector2D(0, -1), direction);

    // Math.acos(direction.y / direction.norm()); // dot product
    if (orderedPosition % 2 == 1) { // 1st, 3rd, 5th.. cars
      position.x = Track.finishLinePoint.x - sepFromLine - HEIGHT / 2 - (orderedPosition - 1) * HEIGHT;
      position.y = Track.finishLinePoint.y + Track.widthTrack / 4;
      // position.y = Track.finishLinePoint.y + W (WIDTH / 2 - Track.widthGridSlot) / 2;
    } else {// 2nd, 4th, 6th.. cars
      position.x = Track.finishLinePoint.x - sepFromLine - HEIGHT / 2 - (orderedPosition - 1) * HEIGHT;
      position.y = Track.finishLinePoint.y + 3 * Track.widthTrack / 4;
      // position.y = Track.finishLinePoint.y + WIDTH / 2 + (WIDTH / 2 - Track.widthGridSlot) / 2;
    }
    initialPosition = position;
  }

  abstract String getCarImagePath();

  abstract void setCarFeatures();

  public double speed() {
    return velocity.norm();
  }

  public double rotationRate() { // function a * x / (b + x^2) because it goes to zero at infinity (no steering wheel is hard to move) and it is zero at the origin (because we can turn the car it it is stopped)
    double scale = 36;
    double displacement = 10;
    return scale * speed() / (displacement + speed() * speed());
  }

  public boolean isOnTrack() {
    return t.area.contains(position.x, position.y);
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
    // velocity = velocity.rotate(rotationRate());
    // velocity = velocity.rotate(rotationRate() / (1 + speed()));
    // isDrifting = Math.abs(Math.toDegrees(difAngle)) > 30;
    // isDrifting = false;
    // velocity = direction.scalarProd(speed());

    // direction = direction.scalarProd(1 / direction.norm()); // the direction hast to be always normalized.
    // if (speed() < TOL)
    // appliedBrakeTorque = 0;
    if (isDrifting) {
      appliedEngineForce *= 0.2;
      appliedBrakeForce *= 0.2;
    }
    Vector2D[] newData = rk4();
    position = newData[0];
    velocity = newData[1];
    // if (isPlayer) {
    // System.out.println("direction: " + direction.print() + " norm: " + direction.norm());
    // System.out.println("position: " + position.print() + " norm: " + position.norm());
    // System.out.println("velocity: " + velocity.print() + " norm: " + speed() + " norm (km/h): " + 3.6 * speed());
    // System.out.println("rotationRate: " + rotationRate());
    // System.out.println("direction: " + direction.print() + " norm: " + direction.norm());
    // System.out.println("rotation: " + rotationAngle);
    // System.out.println("engine force: " + engineForce().print() + " norm: " + engineForce().norm());
    // System.out.println("braking force: " + brakeForce().print() + " norm: " + brakeForce().norm());
    // System.out.println("drag force: " + dragForce().print() + " norm: " + dragForce().norm());
    // System.out.println("friction force: " + rollingResistanceForce().print() + " norm: " + rollingResistanceForce().norm());
    // System.out.println("isOnTrack: " + isOnTrack());
    // }
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

  public void drawCar(Graphics2D g2) {
    if (img == null) {
      g2.setColor(Color.BLACK);
      Path2D.Double path = drawRotatedRectangle(position.x - WIDTH / 2, position.y - HEIGHT / 2, WIDTH, HEIGHT, rotationAngle + initialSteeringAngle);
      g2.fill(path); // oriented towards the right. (-->)
    } else {
      BufferedImage rotImage = rotateAndScaleImage(img, rotationAngle + initialSteeringAngle);
      // double x0 = position.x - rotImage.getWidth() / 2;
      // double y0 = position.y - rotImage.getHeight() / 2;
      // Vector2D v0 = new Vector2D(x0, y0);

      // g2.setColor(Color.WHITE);
      // g2.draw(new Rectangle2D.Double(x0, y0, rotImage.getWidth(), rotImage.getHeight()));
      // v0.rotate(rotationAngle, new Point2D.Double(position.x, position.y));

      // g2.setColor(Color.ORANGE);
      // g2.fill(new Ellipse2D.Double(Track.finishLinePoint.x, Track.finishLinePoint.y, 5, 5));
      // g2.setColor(Color.RED);

      // g2.draw(drawRotatedRectangle(x0, y0, rotImage.getWidth(), rotImage.getHeight(), rotationAngle));
      g2.drawImage(rotImage, (int) Math.round(position.x - rotImage.getWidth() / 2), (int) Math.round(position.y - rotImage.getHeight() / 2), null);
    }
  }

  public Path2D.Double drawRotatedRectangle(double x0, double y0, double width, double height, double angle) {

    Vector2D[] v = new Vector2D[4];
    Vector2D p = new Vector2D(x0 + width / 2, y0 + height / 2);
    v[0] = new Vector2D(x0, y0);
    v[1] = new Vector2D(x0 + width, y0);
    v[2] = new Vector2D(x0 + width, y0 + height);
    v[3] = new Vector2D(x0, y0 + height);

    for (Vector2D vi : v) {
      vi = vi.rotate(angle, p);
    }

    Path2D.Double path = new Path2D.Double();
    path.moveTo(v[0].x, v[0].y);
    for (int i = 1; i < 4; i++) {
      path.lineTo(v[i].x, v[i].y);
    }
    path.closePath();
    return path;
  }

  public BufferedImage rotateAndScaleImage(BufferedImage imageToRotate, double angle) {
    // int oldWidth = imageToRotate.getWidth();
    // int oldHeight = imageToRotate.getHeight();
    int newWidth = (int) Math.round(Math.abs(Math.cos(angle)) * WIDTH + Math.abs(Math.sin(angle)) * HEIGHT);
    int newHeight = (int) Math.round(Math.abs(Math.sin(angle)) * WIDTH + Math.abs(Math.cos(angle)) * HEIGHT);

    BufferedImage newImage = new BufferedImage(newWidth, newHeight, imageToRotate.getType());

    Graphics2D g2 = newImage.createGraphics();
    g2.rotate(angle, newWidth / 2, newHeight / 2);

    g2.drawImage(imageToRotate, newWidth / 2 - WIDTH / 2, newHeight / 2 - HEIGHT / 2, newWidth / 2 + WIDTH / 2, newHeight / 2 + HEIGHT / 2, 0, 0, WIDTH_ORIGINAL, HEIGHT_ORIGINAL, null);
    // public abstract boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer)
    // img - the specified image to be drawn. This method does nothing if img is null.
    // dx1 - the x coordinate of the first corner of the destination rectangle.
    // dy1 - the y coordinate of the first corner of the destination rectangle.
    // dx2 - the x coordinate of the second corner of the destination rectangle.
    // dy2 - the y coordinate of the second corner of the destination rectangle.
    // sx1 - the x coordinate of the first corner of the source rectangle.
    // sy1 - the y coordinate of the first corner of the source rectangle.
    // sx2 - the x coordinate of the second corner of the source rectangle.
    // sy2 - the y coordinate of the second corner of the source rectangle.
    // observer - object to be notified as more of the image is scaled and converted.
    return newImage;
  }
}

class Mercedes extends Car {

  public Mercedes(int orderedPosition, Track t, boolean isPlayer) {
    super(orderedPosition, t, isPlayer);
  }

  @Override
  String getCarImagePath() {
    return "images/car_mercedes.png";
  }

  @Override
  void setCarFeatures() {
    engineMAX = 8000;
    brakeMAX = 10000;
  }
}

class Ferrari extends Car {

  public Ferrari(int orderedPosition, Track t, boolean isPlayer) {
    super(orderedPosition, t, isPlayer);
  }

  @Override
  String getCarImagePath() {
    return "images/car_ferrari.png";
  }

  @Override
  void setCarFeatures() {
    engineMAX = 7500;
    brakeMAX = 11000;
  }

}

class RedBull extends Car {

  public RedBull(int orderedPosition, Track t, boolean isPlayer) {
    super(orderedPosition, t, isPlayer);
  }

  @Override
  String getCarImagePath() {
    return "images/car_redBull.png";
  }

  @Override
  void setCarFeatures() {
    engineMAX = 7000;
    brakeMAX = 12000;
  }

}