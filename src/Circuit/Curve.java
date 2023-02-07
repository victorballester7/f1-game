package Circuit;

import Misc.Vector2D;
import java.awt.geom.*;

public class Curve extends Path implements RelativePosition {
  public Vector2D controlPoint1;
  public Vector2D controlPoint2;
  public Vector2D[] points;
  public CubicCurve2D.Double curve;
  public Vector2D controlPointInner1;
  public Vector2D controlPointInner2;
  public CubicCurve2D.Double curveInner;
  public Vector2D controlPointOuter1;
  public Vector2D controlPointOuter2;
  public CubicCurve2D.Double curveOuter;
  public double forceStart;
  public double forceEnd;
  public Vector2D tangentStart;
  public Vector2D tangentEnd;

  public Curve(Vector2D startPoint, Vector2D endPoint, double forceStart, double forceEnd, Vector2D startPointInner, Vector2D endPointInner, Vector2D startPointOuter, Vector2D endPointOuter) { // we suppose that the circuits are
                                                                                                                                                                                                 // all clockwise
    super(startPoint, endPoint, startPointInner, endPointInner, startPointOuter, endPointOuter);
    this.forceStart = forceStart;
    this.forceEnd = forceEnd;
    Vector2D perpStart = startPointOuter.substract(startPointInner);
    setWidth(perpStart.norm());
    perpStart = perpStart.normalize();
    Vector2D perpEnd = endPointOuter.substract(endPointInner).normalize();
    tangentStart = perpStart.rotate(-Math.PI / 2);
    tangentEnd = perpEnd.rotate(-Math.PI / 2);
    controlPoint1 = startPoint.sum(tangentStart.scalarProd(forceStart)); // to make the joint point smooth
    controlPoint2 = endPoint.sum(tangentEnd.scalarProd(-forceEnd)); // to make the joint point smooth
    points = new Vector2D[] {startPoint.clone(), controlPoint1.clone(), controlPoint2.clone(), endPoint.clone()};
    setCurve();
    createArea();
  }

  public Curve(Vector2D startPoint, Vector2D endPoint, Vector2D diffStart, Vector2D diffEnd, double forceStart, double forceEnd) {
    super(startPoint, endPoint);
    setWidth(0);
    controlPoint1 = startPoint.sum(diffStart.scalarProd(forceStart));
    controlPoint2 = endPoint.sum(diffEnd.scalarProd(-forceEnd));
    points = new Vector2D[] {startPoint.clone(), controlPoint1.clone(), controlPoint2.clone(), endPoint.clone()};
    setCurve();
  }

  public void setCurve() {
    double extraForce = 1.03;
    curve = new CubicCurve2D.Double(startPoint.x, startPoint.y, controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, endPoint.x, endPoint.y);
    if (dimension2) {
      controlPointInner1 = startPointInner.sum(tangentStart.scalarProd(forceStart / extraForce));
      controlPointInner2 = endPointInner.sum(tangentEnd.scalarProd(-forceEnd / extraForce));
      controlPointOuter1 = startPointOuter.sum(tangentStart.scalarProd(forceStart * extraForce));
      controlPointOuter2 = endPointOuter.sum(tangentEnd.scalarProd(-forceEnd * extraForce));
      curveInner = new CubicCurve2D.Double(startPointInner.x, startPointInner.y, controlPointInner1.x, controlPointInner1.y, controlPointInner2.x, controlPointInner2.y, endPointInner.x, endPointInner.y);
      curveOuter = new CubicCurve2D.Double(startPointOuter.x, startPointOuter.y, controlPointOuter1.x, controlPointOuter1.y, controlPointOuter2.x, controlPointOuter2.y, endPointOuter.x, endPointOuter.y);
    }
  }

  public void createArea() {
    boundary = new Path2D.Double(curveOuter);
    boundary.lineTo(endPointInner.x, endPointInner.y);
    boundary.curveTo(controlPointInner2.x, controlPointInner2.y, controlPointInner1.x, controlPointInner1.y, startPointInner.x, startPointInner.y); // we have to reverse the innerCurve
    boundary.closePath();
    area = new Area(boundary);
  }

  public Vector2D bezier(double t) {
    double x = (1 - t) * (1 - t) * (1 - t) * points[0].x + 3 * t * (1 - t) * (1 - t) * points[1].x + 3 * t * t * (1 - t) * points[2].x + t * t * t * points[3].x;
    double y = (1 - t) * (1 - t) * (1 - t) * points[0].y + 3 * t * (1 - t) * (1 - t) * points[1].y + 3 * t * t * (1 - t) * points[2].y + t * t * t * points[3].y;
    return new Vector2D(x, y);
  }

  public Vector2D bezierDiff(double t) {
    double x = 3 * (1 - t) * (1 - t) * (points[1].x - points[0].x) + 6 * (1 - t) * t * (points[2].x - points[1].x) + 3 * t * t * (points[3].x - points[2].x);
    double y = 3 * (1 - t) * (1 - t) * (points[1].y - points[0].y) + 6 * (1 - t) * t * (points[2].y - points[1].y) + 3 * t * t * (points[3].y - points[2].y);
    return new Vector2D(x, y);
  }

  public Vector2D bezierDiff2(double t) {
    double x = 6 * (1 - t) * (points[2].x - 2 * points[1].x + points[0].x) + 6 * t * (points[3].x - 2 * points[2].x + points[1].x);
    double y = 6 * (1 - t) * (points[2].y - 2 * points[1].y + points[0].y) + 6 * t * (points[3].y - 2 * points[2].y + points[1].y);
    return new Vector2D(x, y);
  }

  // the closest point satisfies (p - gamma(t)) · gamma'(t) = 0
  public double f(Vector2D p, double t) {
    return Vector2D.substract(p, bezier(t)).dotProduct(bezierDiff(t));
  }

  // the closest point satisfies: - gamma'(t) · gamma'(t) + (p - gamma(t)) ·
  // gamma''(t) = 0
  public double df(Vector2D p, double t) {
    return -bezierDiff(t).normSq() + Vector2D.substract(p, bezier(t)).dotProduct(bezierDiff2(t));
  }

  private double newton(Vector2D p, double t0) {
    double t = t0;
    double TOL = 1.e-7;
    int MAX_ITER = 100;
    int count;
    for (count = 1; Math.abs(f(p, t)) > TOL && count < MAX_ITER; count++)
      t = t - f(p, t) / df(p, t);
    return t;
  }

  public double getSignedCurvature(double t) {
    double k = bezierDiff(t).det(bezierDiff2(t)) / Math.pow(bezierDiff(t).norm(), 3);
    return k;
  }

  public double getCurvature(double t) {
    return Math.abs(getSignedCurvature(t));
  }

  public boolean toTheRight() { // remember that the y-axis is reversed on the plane.
    // System.out.println("curvature: " + getSignedCurvature(0.5));
    if (getSignedCurvature(0.5) > 0)
      return false;
    else
      return true;
  }

  public boolean toTheLeft() { // remember that the y-axis is reversed on the plane.
    return !toTheRight();
  }

  @Override
  public double getClosestPoint(Vector2D p) {
    return newton(p, 0.5);
  }

  @Override
  public Vector2D positionTo(Vector2D p, double t) {
    return p.substract(position(t));
  }

  @Override
  public Vector2D position(double t) {
    return bezier(t);
  }

  @Override
  public Vector2D tangent(double t) {
    return bezierDiff(t).normalize();
  }

  @Override
  public Vector2D normal(double t) { // normal pointing to the inside of the curve
    Vector2D n = bezierDiff(t).normalize();
    if (getSignedCurvature(t) > 0)
      return n.rotate(Math.PI / 2);
    else
      return n.rotate(-Math.PI / 2);
  }

  // @Override
  // public Shape getPath(Vector2D p) {
  // return curve;
  // }
}
