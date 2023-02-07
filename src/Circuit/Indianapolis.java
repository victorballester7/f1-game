package Circuit;

import Misc.Vector2D;

public class Indianapolis extends Track {
  private static final double longStraight = 1006;
  private static final double shortStraight = 201;
  private static final double curveWidth = 300;
  static public int numPaths = 8;

  public Indianapolis(double widthTrack, double x0, double y0, Vector2D startDirection) {
    super(widthTrack, x0, y0, numPaths, startDirection);
  }

  // Joint point line (Pi) - cubic curve (Qi):
  // Q0 = P1
  // Q1 = (P1 - P0) / 3 + P1 = 4 / 3 * P1 - P0 / 3
  // Q2 = 2 * Q1 - Q0 = 5 / 3 * P1 -
  @Override
  public Path[] setPath() { // assume the first segment is a line
    // double EPS = incr / 3;
    // double incr_curve = widthTrack / 4;
    // double curvature = longStraight / 4;

    Path[] paths = new Path[numPaths];
    Vector2D p0 = new Vector2D(x0, y0);
    Vector2D p1 = new Vector2D(x0 + longStraight, y0);
    double force = 200;

    paths[0] = new Line(p0, p1, widthTrack);

    Path path = paths[0];
    p1.set(x0 + longStraight + curveWidth, y0 - curveWidth);
    paths[1] = new Curve(path.endPoint, p1, force, force, path.endPointInner, new Vector2D(p1.x - widthTrack / 2, p1.y),
        path.endPointOuter, new Vector2D(p1.x + widthTrack / 2, p1.y));

    path = paths[1];
    p1.setY(y0 - curveWidth - shortStraight);
    paths[2] = new Line(path.endPoint, p1, widthTrack);

    path = paths[2];
    p1.set(x0 + longStraight, y0 - 2 * curveWidth - shortStraight);
    paths[3] = new Curve(path.endPoint, p1, force, force, path.endPointInner, new Vector2D(p1.x, p1.y + widthTrack / 2),
        path.endPointOuter, new Vector2D(p1.x, p1.y - widthTrack / 2));

    path = paths[3];
    p1.setX(x0);
    paths[4] = new Line(path.endPoint, p1, widthTrack);

    path = paths[4];
    p1.set(x0 - curveWidth, y0 - curveWidth - shortStraight);
    paths[5] = new Curve(path.endPoint, p1, force, force, path.endPointInner, new Vector2D(p1.x + widthTrack / 2, p1.y),
        path.endPointOuter, new Vector2D(p1.x - widthTrack / 2, p1.y));

    path = paths[5];
    p1.setY(y0 - curveWidth);
    paths[6] = new Line(path.endPoint, p1, widthTrack);

    path = paths[6];
    p1.set(p0);
    paths[7] = new Curve(path.endPoint, p1, force, force, path.endPointInner, new Vector2D(p1.x, p1.y - widthTrack / 2),
        path.endPointOuter, new Vector2D(p1.x, p1.y + widthTrack / 2));
    // int i = 0;
    // for (Path p : paths) {
    // System.out.println("Curve " + i);
    // System.out.println("startPoint: " + p.startPoint);
    // System.out.println("startPointInner: " + p.startPointInner);
    // System.out.println("startPointOuter: " + p.startPointOuter);
    // if (p instanceof Curve) {
    // System.out.println("ControlPoint1: " + ((Curve) p).controlPoint1);
    // System.out.println("ControlPoint2: " + ((Curve) p).controlPoint2);
    // System.out.println("ControlPointInner1: " + ((Curve) p).controlPointInner1);
    // System.out.println("ControlPointInner2: " + ((Curve) p).controlPointInner2);
    // System.out.println("ControlPointOuter1: " + ((Curve) p).controlPointOuter1);
    // System.out.println("ControlPointOuter2: " + ((Curve) p).controlPointOuter2);
    // System.out.println("tangentStart: " + ((Curve) p).tangentStart);
    // System.out.println("tangentEnd: " + ((Curve) p).tangentEnd);
    // }
    // System.out.println("EndPoint: " + p.endPoint);
    // System.out.println("endPointInner: " + p.endPointInner);
    // System.out.println("endPointOuter: " + p.endPointOuter);
    // i++;
    // }
    return paths;
  }

  @Override
  public Path[] setBestPath() {
    // double EPS = incr / 3;
    // double incr_curve = incr + EPS;
    double margin = widthTrack / 5;
    double extra = widthTrack / 2 - margin;

    Path[] paths = new Path[numPaths];
    Vector2D p0 = new Vector2D(x0, y0 + extra);
    Vector2D p1 = new Vector2D(x0 + longStraight, y0 + extra);
    Vector2D diff0 = new Vector2D(1, 0);
    Vector2D diff1 = new Vector2D(0, -1);
    double force = 180;

    paths[0] = new Line(p0, p1);

    Path path = paths[0];
    p1.set(x0 + longStraight + curveWidth + extra, y0 - curveWidth);
    paths[1] = new Curve(path.endPoint, p1, diff0, diff1, force, force);

    path = paths[1];
    p1.setY(y0 - curveWidth - shortStraight);
    paths[2] = new Line(path.endPoint, p1);

    path = paths[2];
    p1.set(x0 + longStraight, y0 - 2 * curveWidth - shortStraight - extra);
    diff0 = diff1.clone();
    diff1.set(-1, 0);
    paths[3] = new Curve(path.endPoint, p1, diff0, diff1, force, force);

    path = paths[3];
    p1.setX(x0);
    paths[4] = new Line(path.endPoint, p1);

    path = paths[4];
    p1.set(x0 - curveWidth - extra, y0 - curveWidth - shortStraight);
    diff0 = diff1.clone();
    diff1.set(0, 1);
    paths[5] = new Curve(path.endPoint, p1, diff0, diff1, force, force);

    path = paths[5];
    p1.setY(y0 - curveWidth);
    paths[6] = new Line(path.endPoint, p1);

    path = paths[6];
    p1.set(p0);
    diff0 = diff1.clone();
    diff1.set(1, 0);
    paths[7] = new Curve(path.endPoint, p1, diff0, diff1, force, force);

    return paths;
  }

  @Override
  void setFinishLinePoint() {
    finishLinePoint.x = x0 + 3 * longStraight / 4;
    finishLinePoint.y = y0 - widthTrack / 2;
  }

}