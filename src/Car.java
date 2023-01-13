import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;

abstract public class Car {
  public static final double TOL = 0.001;
  public static final int WIDTH_ORIGINAL = 38;
  public static final int HEIGHT_ORIGINAL = 2 * WIDTH_ORIGINAL;
  public static final int WIDTH = 20;
  public static final int HEIGHT = 2 * WIDTH;
  public static final double rotationSpeed = Math.toRadians(2);
  public int orderedPosition; // 1st, 2nd, ...
  public double rotationAngle = Math.toRadians(0);
  public double slipRatio;
  public Vector2D direction = new Vector2D(1, 0);
  public Vector2D position = new Vector2D();
  public Vector2D initialPosition = new Vector2D();
  public Vector2D velocity = new Vector2D();
  public Vector2D acceleration = new Vector2D();
  public double angularVelocity = 0;
  public BufferedImage img;
  private boolean isPlayer;
  private static final double ENGINE_CONSTANT = 3;
  private static final double WHEEL_RADIUS = 28 / 2 * 0.0254; // 18 - inch tyres
  private static final double Cd = 0.7; // drag coefficient
  private static final double Cr = 30 * Cd; // rolling coefficient
  private static final double Ct = 1; // traction coefficient
  private static final double mu = 1.5; // friction coefficient of the tyre
  private static final double g = 9.807; // gravity
  private static final double AIR_DENSITY = 1.204; // air density
  private static final double AREA = 1.5; // cross sectional area
  private static final double MASS = 750; // mass of the car
  private static final double MASS_WHEEL = 13; // mass of the car
  private static final double MOMENT_OF_INERTIA = MASS_WHEEL * WHEEL_RADIUS * WHEEL_RADIUS / 2; // moment of inertia for a cylinder
  public double appliedEngineTorque = 0; // 0 or MAX_ENGINE_TORQUE
  public double MAX_ENGINE_TORQUE; // between 600 and 800
  public double appliedBrakeTorque; // 0 or MAX_BRAKING_COEF
  public double MAX_BRAKING_COEF; // ~ 8000

  public Car(int orderedPosition, boolean isPlayer) {
    this.orderedPosition = orderedPosition;
    this.isPlayer = isPlayer;
    img = setCar();
    setInitialPosition();
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

  public void setInitialPosition() {
    int sepFromLine = 10;
    rotationAngle = Math.signum(direction.x) * Vector2D.angle(direction, new Vector2D(0, 1)); // both are unit vectors

    // Math.acos(direction.y / direction.norm()); // dot product
    if (orderedPosition % 2 == 1) { // 1st, 3rd, 5th.. cars
      position.x = Track.finishLinePoint.x - sepFromLine - HEIGHT / 2 - (orderedPosition - 1) * HEIGHT;
      position.y = Track.finishLinePoint.y + Track.WIDTH / 4;
      // position.y = Track.finishLinePoint.y + W (WIDTH / 2 - Track.widthGridSlot) / 2;
    } else {// 2nd, 4th, 6th.. cars
      position.x = Track.finishLinePoint.x - sepFromLine - HEIGHT / 2 - (orderedPosition - 1) * HEIGHT;
      position.y = Track.finishLinePoint.y + 3 * Track.WIDTH / 4;
      // position.y = Track.finishLinePoint.y + WIDTH / 2 + (WIDTH / 2 - Track.widthGridSlot) / 2;
    }
    initialPosition.setCoords(position.x, position.y);
  }

  abstract String getCarImagePath();

  abstract void setCarFeatures();

  public void update() {
    // rotationAngle = Math.signum(direction.x) * Math.acos(direction.dotProduct(velocity) /( direction.norm() * velocity.norm())); // dot product

    // reset direction parameters.
    direction.x = 0;
    direction.y = -1;
    direction = direction.rotate(rotationAngle);

    velocity = direction.scalarProd(velocity.norm());

    // direction = direction.scalarProd(1 / direction.norm()); // the direction hast to be always normalized.
    // if (velocity.norm() < TOL)
    // appliedBrakeTorque = 0;

    double[] newData = rk4();
    position.setCoords(newData[0], newData[1]);
    velocity.setCoords(newData[2], newData[3]);
    angularVelocity = Math.max(0, newData[4]);
    if (orderedPosition == 1) {
      System.out.println("velocity: " + velocity.print() + " norm: " + velocity.norm());
      System.out.println("angularVelocity: " + angularVelocity);
      System.out.println("angularVelocity (difference): " + (angularVelocity * WHEEL_RADIUS - velocity.norm()));
      System.out.println("direction: " + direction.print() + " norm: " + direction.norm());
      System.out.println("rotation: " + rotationAngle);
      System.out.println("engine torque: " + engineTorque());
      System.out.println("braking torque: " + -brakingTorque(velocity));
      System.out.println("traction torque: " + tractionForce(velocity, angularVelocity) * WHEEL_RADIUS);
      System.out.println("drag force: " + -dragForce() * velocity.norm() * velocity.x);
      System.out.println("friction force: " + -rollingResistanceForce() * velocity.x);
    }
    // direction.rotate(rotationAngle, position);
    // rotationAngle += Math.toRadians(20);
    // position.rotate(rotationAngle);
  }

  private double[] systemODEs(double[] X) {
    // think if I have to vary the direction too
    // Vector2D r = X[0];
    Vector2D v = new Vector2D(X[2], X[3]);
    double omega = X[4];
    Vector2D rDot = new Vector2D();
    Vector2D vDot = new Vector2D();
    double omegaDot; // we only store the value at omega.x, so we treat it as a double. We do it in this way to simplify the arguments an returns of the functions involved
    rDot.x = v.x / Track.metersPerPixel;
    rDot.y = v.y / Track.metersPerPixel;

    vDot.x = (direction.x * engineTorque() / WHEEL_RADIUS - rollingResistanceForce() * v.x - dragForce() * v.norm() * v.x - brakingTorque(v) * direction.x / WHEEL_RADIUS + tractionForce(v, omega) * direction.x) / MASS;
    vDot.y = (direction.y * engineTorque() / WHEEL_RADIUS - rollingResistanceForce() * v.y - dragForce() * v.norm() * v.y - brakingTorque(v) * direction.y / WHEEL_RADIUS + tractionForce(v, omega) * direction.y) / MASS;

    omegaDot = (engineTorque() - brakingTorque(v) - tractionForce(v, omega) * WHEEL_RADIUS) / (3 * MOMENT_OF_INERTIA);

    // if (orderedPosition == 1) {
    // System.out.println("vDot: " + vDot.print());
    // }
    return new double[] {rDot.x, rDot.y, vDot.x, vDot.y, omegaDot};
  }

  private double engineTorque() {
    return appliedEngineTorque * ENGINE_CONSTANT;
  }

  private double tractionForce(Vector2D v, double omega) { // force done by friction force (it is a linear approximation)
    if (Math.abs(omega) < TOL)
      return 0;
    return Math.min(Ct * (omega * WHEEL_RADIUS - v.norm()) / v.norm(), mu * MASS * g);
  }

  private double brakingTorque(Vector2D v) {
    if (v.norm() < TOL)
      return 0;
    return appliedBrakeTorque;
  }

  private double dragForce() {
    return 0.5 * Cd * AREA * AIR_DENSITY;
  }

  private double rollingResistanceForce() {
    return Cr;
  }

  private double[] rk4() { // Runge - Kutta 4
    double STEP = 0.03;
    double[] initialPoints = new double[] {position.x, position.y, velocity.x, velocity.y, angularVelocity};

    // Define the k's of the Runge - Kutta.
    double[] k1 = systemODEs(initialPoints);
    double[] k2 = systemODEs(myMath.sum(initialPoints, myMath.scalarProd(STEP / 2, k1)));
    double[] k3 = systemODEs(myMath.sum(initialPoints, myMath.scalarProd(STEP / 2, k2)));
    double[] k4 = systemODEs(myMath.sum(initialPoints, myMath.scalarProd(STEP, k3)));

    // Evaluate the new position.
    // avg = Vector2D.scalarProd(Vector2D.sum(k1, Vector2D.scalarProd(k2, 2), Vector2D.scalarProd(k3, 2), k4), 1 / 6);
    // a = avg[1]; // acceleration = dv/dt
    // finalPoints = Vector2D.sum(initialPoints, Vector2D.scalarProd(avg, STEP));
    double[] finalPoints = myMath.sum(initialPoints, myMath.scalarProd(STEP / 6, myMath.sum(k1, myMath.scalarProd(2, k2), myMath.scalarProd(2, k3), k4)));

    // for (int i = 0; i < 5; i++) {
    // finalPoints[i] = initialPoints.x + (k1[i] + k2[i] + 2 * k3[i] + 2 * k4[i]) * STEP / 6;
    // }

    // if (orderedPosition == 1) {
    // System.out.println("initialPoints_x: " + initialPoints[0].print());
    // System.out.println("finalPoints_x: " + finalPoints[0].print());
    // System.out.println("initialPoints_v: " + initialPoints[1].print());
    // System.out.println("finalPoints_v: " + finalPoints[1].print());
    // }
    // finalPoints[3] = finalPoints[2]; // we move the omega into the 3rd component
    // finalPoints[2] = a;
    // Vector2D rDot = position.sum(new Vector2D().sum(new Vector2D[] {k1[0], k2[0].scalarProd(2), k3[0].scalarProd(2), k4[0]}).scalarProd(STEP / 6));
    // Vector2D vDot = velocity.sum(new Vector2D().sum(new Vector2D[] {k1[1], k2[1].scalarProd(2), k3[1].scalarProd(2), k4[1]}).scalarProd(STEP / 6));

    return finalPoints;
  }

  public void drawCar(Graphics2D g2) {
    if (img == null) {
      g2.setColor(Color.BLACK);
      Path2D.Double path = drawRotatedRectangle(position.x - WIDTH / 2, position.y - HEIGHT / 2, WIDTH, HEIGHT, rotationAngle);
      g2.fill(path); // oriented towards the right. (-->)
    } else {
      BufferedImage rotImage = rotateAndScaleImage(img, rotationAngle);
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

  public Mercedes(int orderedPosition, boolean isPlayer) {
    super(orderedPosition, isPlayer);
  }

  @Override
  String getCarImagePath() {
    return "images/car_mercedes.png";
  }

  @Override
  void setCarFeatures() {
    MAX_ENGINE_TORQUE = 800;
    MAX_BRAKING_COEF = 8000;
  }
}

class Ferrari extends Car {

  public Ferrari(int orderedPosition, boolean isPlayer) {
    super(orderedPosition, isPlayer);
  }

  @Override
  String getCarImagePath() {
    return "images/car_ferrari.png";
  }

  @Override
  void setCarFeatures() {
    MAX_ENGINE_TORQUE = 750;
    MAX_BRAKING_COEF = 9000;
  }

}

class RedBull extends Car {

  public RedBull(int orderedPosition, boolean isPlayer) {
    super(orderedPosition, isPlayer);
  }

  @Override
  String getCarImagePath() {
    return "images/car_redBull.png";
  }

  @Override
  void setCarFeatures() {
    MAX_ENGINE_TORQUE = 700;
    MAX_BRAKING_COEF = 10000;
  }

}