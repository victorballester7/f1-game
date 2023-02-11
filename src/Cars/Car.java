package Cars;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;

import Circuit.*;
import Main.Game;
import Misc.Vector2D;

abstract public class Car {
  public static final double TOL = 0.001;
  public static int lengthOriginal;
  public static int widthOriginal;
  public static final double length = 5.5;
  public static final double width = 2;
  public int id;
  public int orderedPosition; // 1st, 2nd, ...
  public int finalPosition; // 1st, 2nd, ...
  public int lapCounter = -1;
  public static double initialSteeringAngle;
  public double rotationAngle = Math.toRadians(0);
  private double forceReincorporationLine = 0.01;
  private double forceReincorporationCurve = 0.1;
  public static Track t;
  public Vector2D direction = new Vector2D(1, 0);
  public Vector2D position = new Vector2D();
  public Vector2D initialPosition = new Vector2D();
  public Vector2D velocity = new Vector2D();
  public BufferedImage img;
  public boolean isPlayer;
  public Color color;
  public Color color2;
  private static final double mu = 1.5; // friction coefficient
  private static final double Cd = 0.7; // drag coefficient
  private static final double CrTrack = 30 * Cd; // rolling coefficient track
  private static final double CrWeed = 30 * CrTrack; // rolling coefficient weed
  private static final double airDensity = 1.204; // air density
  private static final double area = 1.5; // cross sectional area
  private static final double m = 750; // mass of the car
  private static final double g = 9.81; // gravity
  // private static final double drift = 10; // drift constant
  public double appliedEngineForce = 0; // 0 or engineMAX
  public double engineMAX; // between 600 and 800
  public double appliedBrakeForce; // 0 or brakeMAX
  public double brakeMAX; // ~ 8000
  private boolean isBraking = false;
  private boolean isDrifting;
  public int currentPathIndex;
  public int totalPathsDone = 0;

  public Car(int id, Track t, boolean isPlayer) {
    this.id = id;
    orderedPosition = id;
    Car.t = t;
    this.isPlayer = isPlayer;
    currentPathIndex = 0;
    img = setImgCar();
    lengthOriginal = img.getWidth(null);
    widthOriginal = img.getHeight(null);
    position = setInitialPosition(t, orderedPosition);
    initialPosition = position.clone();
    setCarFeatures();
    if (!isPlayer) {
      engineMAX = engineMAX * Game.levelOfDifficulty();
      brakeMAX = brakeMAX * Game.levelOfDifficulty();
    }
  }

  private BufferedImage setImgCar() {
    BufferedImage img = null;
    // BufferedImage img = new BufferedImage(width, height,
    // BufferedImage.TYPE_INT_ARGB);
    ;
    String fileName = getCarImagePath();
    try {
      img = ImageIO.read(new File(fileName));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return img;
  }

  public static Vector2D setInitialPosition(Track t, int orderedPosition) {
    double sepFromLine = length * 1.5;
    initialSteeringAngle = Vector2D.signedAngle(new Vector2D(0, -1), t.startDirection);
    double x, y;

    x = t.finishLinePoint.x - sepFromLine - length / 2 - (orderedPosition - 1) * length;

    if (orderedPosition % 2 == 1) { // 1st, 3rd, 5th.. cars
      y = t.finishLinePoint.y + 3 * t.widthTrack / 4;
    } else {// 2nd, 4th, 6th.. cars
      y = t.finishLinePoint.y + t.widthTrack / 4;
    }
    return new Vector2D(x, y);
  }

  abstract public String getCarImagePath();

  abstract public void setCarFeatures();

  public double speed() {
    return velocity.norm();
  }

  public double rotationRate() { // function a * x / (b + x^2) because it goes to zero at infinity (no steering
                                 // wheel is hard to move) and it is zero at the origin (because we can turn the
                                 // car it it is stopped)
    // it is in degree
    // double scale = 7.5;
    // double displacement = 20;
    // return scale * speed() / (displacement + speed() * speed());
    // double scale = 7.5;
    // double displacement = 20;
    // return scale * speed() / (displacement + speed() * speed());
    double speedMaxRotationRate = 40;
    double scale1 = 1. / speedMaxRotationRate;
    double scale2 = 0.1;
    // double displacement = 20;
    if (speed() < speedMaxRotationRate)
      return scale1 * speed();
    else
      return 1. / (scale2 * (speed() - speedMaxRotationRate) + 1);
  }

  public boolean isOnTrack() {
    return t.area.contains(position.x, position.y);
  }

  public boolean isBehind(Car c) {
    if (totalPathsDone < c.totalPathsDone)
      return true;
    else if (totalPathsDone > c.totalPathsDone)
      return false;
    else {
      // I know, there can be cases in which this is not a sufficient condition (for example a very closed curved). But in general let's assume that it's tue.
      if (position.distance(nextPath().startPoint) > c.position.distance(nextPath().startPoint))
        return true;
      else
        return false;
    }
  }

  public boolean isInLine() {
    return t.paths[currentPathIndex] instanceof Line;
  }

  public Path nextPath() {
    if (currentPathIndex < t.numPaths - 1)
      return t.paths[currentPathIndex + 1];
    else
      return t.paths[0];
  }

  // public Path nextBestPath() {
  // if (currentPathIndex < t.numPaths - 1)
  // return t.pathsBestTrajectory[currentPathIndex + 1];
  // else
  // return t.pathsBestTrajectory[0];
  // }

  public double speedMax(Curve c, double time) { // we assume that we are in a curve
    return Math.sqrt(mu * g / c.getCurvature(time));
  }

  private double f(double v) {
    Vector2D newVelocity;
    if (speed() < TOL)
      newVelocity = new Vector2D();
    else
      newVelocity = velocity.normalize().scalarProd(v);
    // System.out.println("brake force: " + Vector2D.sum(brakeForce(),
    // dragForce(newVelocity), rollingResistanceForce(newVelocity)).norm());
    return Math.abs(m * v / Vector2D.sum(brakeForce(), dragForce(newVelocity), rollingResistanceForce(newVelocity)).norm());
  }

  private double simpsonMethod(double a, double b) {
    int N = (int) Math.ceil(Math.abs((b - a) * 10));
    double h = Math.abs((b - a) / N), integral = 0;
    for (int i = 1; i < N / 2; i++) {
      // System.out.println("value: " + (2 * f(a + h * 2 * i) + 4 * f(a + h * (2 * i -
      // 1))));
      integral += 2 * f(a + h * 2 * i) + 4 * f(a + h * (2 * i - 1));
    }
    // System.out.println("(a, b) = (" + a + ", " + b + ")");
    // System.out.println("value2: " + (4 * f(a + h * (N - 1)) + f(a) + f(b)) + "
    // and N = " + N);
    integral += 4 * f(a + h * (N - 1)) + f(a) + f(b);
    return h * integral / 3;
  }

  public boolean hasToBrake() { // we assume that we are in a line.
    Curve c = (Curve) nextPath();
    double sepTime = 0.5; // middle of the curve
    // System.out.println(getClass() + ": We are inside. speed: " + speed() + ",
    // speedMax: " + speedMax(c, sepTime));
    if (speed() < speedMax(c, sepTime)) // this solves the problem of the first curve of the race
      return false;
    appliedBrakeForce = brakeMAX;
    double brakingDistance = simpsonMethod(speed(), speedMax(c, sepTime));
    appliedBrakeForce = 0;
    // System.out.println(getClass() + ": We are inside. brakingDistance: " +
    // brakingDistance + ", distance to nextCurve: " +
    // position.distance(c.startPoint) + ", speed: " + speed() + ", speedMax: " +
    // speedMax(c, sepTime));
    if (position.distance(c.startPoint) > brakingDistance)
      return false;
    else
      return true;
  }

  public void reincorporate(Path p, double time, double force) {
    direction = p.tangent(time).sum(p.positionTo(position, time).scalarProd(-force)).normalize();
    // System.out.println("tangent: " + p.tangent(time));
  }

  public boolean hasFinished() {
    if (lapCounter == Game.totalLaps)
      return true;
    else
      return false;
  }

  public void update() {
    // rotationAngle = Math.signum(direction.x) *
    // Math.acos(direction.dotProduct(velocity) /( direction.norm() * speed())); //
    // dot product

    // reset direction parameters.
    // direction.x = 0;
    // direction.y = -1;
    // direction = direction.rotate(rotationAngle);
    for (int i = 0; i < t.numPaths; i++) {
      // note that if we are out of the track at this moment, 'currentPathIndex' will
      // be equal to the last 'currentPathIndex' stored.
      if (t.paths[i].area.contains(position.toPoint())) {
        if (currentPathIndex != i) {
          totalPathsDone++;
          currentPathIndex = i;
        }
        break;
      }
    }
    Path path = t.pathsBestTrajectory[currentPathIndex];
    double time = path.getClosestPoint(position);

    if (!isPlayer) {
      // System.out.println("No soc jugador! " + getClass());
      // if (!t.area.contains(position.toPoint())) // if it is out of the track, point
      // perpendicularly to the closest position of the track.
      // direction = (path.positionTo(position, time).scalarProd(-1)).normalize();
      // else {
      if (isInLine()) {
        // System.out.println(getClass() + ": I'm in the line ");
        // if (lapCounter == 0 && currentPathIndex == 0) { // first brake of the race
        // (for the first turn)
        // if (position.distance(nextBestPath().startPoint) >
        // initialBrakingPoint.distance(nextBestPath().startPoint)) {
        reincorporate(path, time, forceReincorporationLine);
        // appliedEngineForce = engineMAX;
        // appliedBrakeForce = 0;
        // } else {
        // appliedEngineForce = 0;
        // appliedBrakeForce = brakeMAX;
        // }
        // }
        // else { // general braking part
        // System.out.println(getClass() + "--> Current speed: " + speed());
        // System.out.println(getClass() + "--> braking: " + isBraking);
        if (!isBraking) {
          if (hasToBrake()) {
            isBraking = true;
            appliedEngineForce = 0;
            appliedBrakeForce = brakeMAX;
          } else {
            // if (appliedEngineForce > 0)
            // System.out.println("Comencem a frenar: " + position);
            appliedEngineForce = engineMAX;
            appliedBrakeForce = 0;
          }
        } else { // just in case
          appliedEngineForce = 0;
          appliedBrakeForce = brakeMAX;
        }
        // }
      } else { // is in curve
        // if (isBraking)
        // System.out.println(getClass() + " -> entering speed: " + speed());
        isBraking = false;
        // System.out.println("I'm in the curve" + getClass());
        Curve c = (Curve) path;
        reincorporate(path, time, forceReincorporationCurve);
        // if (c.toTheRight() && Vector2D.signedAngle(c.tangent(time), direction) > 0) {
        // direction = direction.rotate(-Math.toRadians(rotationRate()));
        // } else if (!c.toTheRight() && Vector2D.signedAngle(c.tangent(time),
        // direction) < 0)
        // direction = direction.rotate(Math.toRadians(rotationRate()));
        // direction = c.tangent(time);
        // direction = c.tangent(time).sum(c.normal(time).scalarProd(steeringForce *
        // (speed()))).normalized();
        // direction =
        // c.tangent(time).scalarProd(speed()).sum(c.normal(time).scalarProd(steeringForce
        // * speed() * speed() * c.getCurvature(time))).normalized();
        // // we correct the possible deviation of the car with respect to the best
        // line: velocity -> tangent + (bestLinePoint - position)
        // velocity =
        // c.tangent(time).scalarProd(speed()).sum(Vector2D.substract(c.bezier(time),
        // position)); // we correct the possible deviation of the car with respect to
        // the best line: velocity -> tangent + (bestLinePoint - position)

        // System.out.println("Inside the curve:" + time + " speed: " + speed() + "
        // speedMAX: " + speedMax(c, time) + "direction" + direction);
        if (speed() < speedMax(c, time) * Game.levelOfDifficulty()) {
          appliedEngineForce = engineMAX;
          appliedBrakeForce = 0;
        } else {
          appliedEngineForce = 0;
          appliedBrakeForce = 0;
        }
      }
      // }
    }

    rotationAngle = Vector2D.signedAngle(t.startDirection, direction);

    // double dif = 0.05 * (speed() / 30.0);
    // double difAngle = Vector2D.signedAngle(velocity, direction);
    // if (!Double.isNaN(difAngle)) {
    // // velocity = velocity.rotate(difAngle / Math.toRadians(drift * rotationRate
    // * speed()));
    // double r = Math.random() * 50;
    // // velocity.rotateZ(difAngle / (100 * (5 * dif)));
    // velocity = velocity.rotate(difAngle / ((50 + r) * (5 * dif)));
    // // isDrifting = false;
    // }
    // velocity = velocity.rotate(difAngle / Math.toRadians(drift * rotationRate *
    // speed()));
    // velocity.rotateZ(difAngle / (100 * (5 * dif)));
    double angle = Vector2D.signedAngle(velocity, direction);
    if (!Double.isNaN(angle)) {
      velocity = velocity.rotate(angle / (speed()));
      isDrifting = Math.abs(Math.toDegrees(angle)) > 10;
    }
    if (isDrifting) {
      appliedEngineForce *= 0.2;
      appliedBrakeForce *= 0.2;
    }

    if (hasFinished()) {
      appliedEngineForce = 0;
      appliedBrakeForce = brakeMAX;
    }

    Vector2D[] newData = rk4();

    if (t.areaSquaresfinishLine.contains(position.toPoint()) && !t.areaSquaresfinishLine.contains(newData[0].toPoint()))
      lapCounter++;

    if (hasFinished())
      finalPosition = orderedPosition;

    position = newData[0].clone();
    velocity = newData[1].clone();

    // if (isPlayer) {
    // System.out.println("direction: " + direction + " norm: " + direction.norm());
    // System.out.println("position: " + position + " norm: " + position.norm());
    // System.out.println("velocity: " + velocity + " norm: " + speed() + " norm(km/h): " + 3.6 * speed());
    // System.out.println("rotationRate: " + rotationRate());
    // System.out.println("direction: " + direction + " norm: " + direction.norm());
    // System.out.println("rotation: " + rotationAngle);
    // System.out.println("engine force: " + engineForce() + " norm: " + engineForce().norm());
    // System.out.println("braking force: " + brakeForce() + " norm: " + brakeForce().norm());
    // System.out.println("drag force: " + dragForce(velocity) + " norm: " + dragForce(velocity).norm());
    // System.out.println("friction force: " + rollingResistanceForce(velocity) + " norm: " + rollingResistanceForce(velocity).norm());
    // System.out.println("isOnTrack: " + isOnTrack());
    // System.out.println("isOnBestTrack: " + t.area.contains(position.toPoint()));
    // System.out.println("lapCounter: " + lapCounter);
    // System.out.println("orderedPosition: " + orderedPosition);
    // System.out.println("currentPathIndex: " + currentPathIndex);
    // System.out.println("Tangent: " + path.tangent(time));
    // System.out.println("comparisonOrientationCurve: " + (Vector2D.signedAngle(path.tangent(time), direction) > 0));
    // }
    // direction.rotate(rotationAngle, position);
    // rotationAngle += Math.toRadians(20);
    // position.rotate(rotationAngle);
  }

  private Vector2D[] systemODEs(Vector2D[] X) {
    Vector2D rDot = X[1]; // X[1] velocity
    Vector2D vDot = acceleration(X[1]);

    return new Vector2D[] {rDot, vDot};
  }

  private Vector2D acceleration(Vector2D v) {
    return Vector2D.sum(engineForce(), brakeForce(), dragForce(v), rollingResistanceForce(v)).scalarProd(1 / m);
  }

  private Vector2D engineForce() {
    return direction.scalarProd(appliedEngineForce);
  }

  // private Vector2D brakeForce(Vector2D v) {
  // if (v.norm() < TOL)
  // return new Vector2D();
  // return v.normalized().scalarProd(-appliedBrakeForce);
  // }

  private Vector2D brakeForce() {
    return direction.scalarProd(-appliedBrakeForce);
  }

  private Vector2D dragForce(Vector2D v) {
    return v.scalarProd(-0.5 * Cd * area * airDensity * v.norm());
  }

  private Vector2D rollingResistanceForce(Vector2D v) {
    if (isOnTrack())
      return v.scalarProd(-CrTrack);
    else
      return v.scalarProd(-CrWeed);
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
