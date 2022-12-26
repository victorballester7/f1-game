import java.awt.*;
import java.awt.geom.*;
import java.awt.geom.Point2D;

import javax.swing.*;

// abstract class Segment extends Track {
// Point2D p01;
// int length;
// double orientationAngle;

// public Segment(Point2D p01, int length, double angleOrientation) {
// this.p01 = p01;
// this.length = length;
// this.orientationAngle = angleOrientation;
// }

// public Point2D[] getInitialPoints() {
// int new_x = p01.x + width;
// int new_y = p01.y;
// Point2D p02 = new Point2D(new_x, new_y);
// Point2D[] p = new Point2D[] {p01, p02.rotate(orientationAngle)};
// return p;
// }

// abstract public Point2D[] getEndPoints();
// }

// class StraightLine extends Segment {
// public StraightLine(Point2D p01, int length, double angleOrientation) {
// super(p01, length, angleOrientation);
// }

// @Override
// public Point2D[] getEndPoints() {
// Point2D p11 = new Point2D(p01.x, p01.y + length);
// Point2D p12 = new Point2D(p01.x + width, p01.y + length);
// Point2D[] p = new Point2D[] {p11.rotate(orientationAngle), p12.rotate(orientationAngle)};
// return p;
// }

// }

// class CurveClockWise extends Segment {
// int SteeringAngle;

// public CurveClockWise(Point2D p01, int length, double angleOrientation, int SteeringAngle) {
// super(p01, length, angleOrientation);
// this.SteeringAngle = SteeringAngle;
// }

// @Override
// public Point2D[] getEndPoints() {
// double centralRadius = length / SteeringAngle;
// double innerRadius = centralRadius - width / 2;
// double outerRadius = centralRadius + width / 2;
// int new_x = p01.x + width;
// int new_y = p01.y;
// Point2D p02 = new Point2D(new_x, new_y);
// Point2D p11 = p01.rotate(-SteeringAngle);
// Point2D p12 = p[1].rotate(-SteeringAngle);
// Point2D[] p_rot = new Point2D[] {p11.rotate(orientationAngle), p12.rotate(orientationAngle)};
// return p_rot;
// }
// }
// // public StraightLine(Point2D p1, Point2D p2, int length) {
// // super(p1, p2, length);
// // }
// // }
// ------------------------------------
abstract public class Track {
  // we assume by the moment that all tracks start in a straight line with a long straight pointing to the right --->.
  public static double WIDTH;
  public static Point2D.Double finishLinePoint = new Point2D.Double();
  public static int widthFinishLine = 25;
  public static int marginGridSlot = 2;
  public static int widthGridSlot = Car.WIDTH + 2 * marginGridSlot;
  public static int lengthGridSlot = Car.HEIGHT / 2;
  public Path2D.Double path = new Path2D.Double();
  public Path2D.Double innerPath = new Path2D.Double();
  public Path2D.Double outerPath = new Path2D.Double();
  public Area area;
  public Color trackColor = new Color(100, 100, 100);

  // public Track(double x0, double y0) {
  // setPath(path, x0, y0, 0);
  // setPath(innerPath, x0, y0, -width / 2);
  // setPath(outerPath, x0, y0, width / 2);
  // }

  // public Track(int width, double x0, double y0) {
  // this.width = width;
  // setPath(path, x0, y0, 0);
  // setPath(innerPath, x0, y0, -width / 2);
  // setPath(outerPath, x0, y0, width / 2);
  // area = new Area(outerPath);
  // area.subtract(new Area(innerPath));
  // }

  public Track() {
  }

  abstract void setPath(Path2D path, double x0, double y0, double incr);

  abstract void setFinishLinePoint();

  private void paintFinishLine(Graphics2D g2) {
    int verticalNumSquares = 20;
    double sideLength = WIDTH / verticalNumSquares;
    int horizontalNumSquares = (int) Math.round(widthFinishLine / sideLength);
    for (int i = 0; i < horizontalNumSquares; i++) {
      for (int j = 0; j < verticalNumSquares; j++) {
        if ((i + j) % 2 == 1)
          g2.setColor(Color.WHITE);
        else
          g2.setColor(Color.BLACK);
        g2.fill(new Rectangle2D.Double(finishLinePoint.x + i * sideLength, finishLinePoint.y + j * sideLength, sideLength, sideLength));
      }
    }
  }

  private void paintGrid(Car[] cars, Graphics2D g2) {
    paintFinishLine(g2);
    // for (int i = 0; i < Game.NumCars; i++) {
    for (Car c : cars) {
      paintCarGridSlot(new Point2D.Double(c.position.x + Car.HEIGHT / 2, c.position.y - Car.WIDTH / 2), g2);
    }
  }

  private void paintCarGridSlot(Point2D.Double p, Graphics2D g2) {
    // Point p is the one marked with a circle in the figure below.
    // Ignore the points in the middle (think they are invisible)
    // ---o
    // ...|
    // ...|
    // ...|
    // ...|
    // ----
    double x0 = p.x + marginGridSlot;
    double y0 = p.y - marginGridSlot;
    Path2D.Double path = new Path2D.Double();
    path.moveTo(x0 - lengthGridSlot, y0);
    path.lineTo(x0, y0);
    path.lineTo(x0, y0 + widthGridSlot);
    path.lineTo(x0 - lengthGridSlot, y0 + widthGridSlot);
    g2.setColor(Color.WHITE);
    g2.draw(path);
  }

  public void drawTrack(Graphics2D g2) {
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // better resolution
    g2.setColor(trackColor);
    g2.fill(area);
    paintGrid(Game.cars, g2);
  }
}

class OvalTrack extends Track {
  private double straightLength = 800, curveWidth = 400;
  private double x0, y0;

  public OvalTrack(double WIDTH, double x0, double y0) {
    Track.WIDTH = WIDTH;
    this.x0 = x0;
    this.y0 = y0;
    setPath(path, x0, y0, 0);
    setPath(innerPath, x0, y0, -WIDTH / 2);
    setPath(outerPath, x0, y0, WIDTH / 2);
    area = new Area(outerPath);
    area.subtract(new Area(innerPath));
    setFinishLinePoint();
  }

  // @Override
  // void setPath(double x0, double y0) {
  // path.moveTo(x0, y0);
  // path.lineTo(x0 + straightLength, y0);
  // path.curveTo(x0 + straightLength + 200, y0, x0 + straightLength + 200, y0 + curveWidth, x0 + straightLength, y0 + curveWidth);
  // path.lineTo(x0, y0 + curveWidth);
  // path.curveTo(x0 - 200, y0 + curveWidth, x0 - 200, y0, x0, y0);
  // path.closePath();
  // }

  @Override
  void setPath(Path2D path, double x0, double y0, double incr) {
    double EPS = incr / 3;
    double incr_curve = incr + EPS;
    path.moveTo(x0, y0 - incr);
    path.lineTo(x0 + straightLength, y0 - incr);
    path.curveTo(x0 + straightLength + 200 + incr_curve, y0 - incr, x0 + straightLength + 200 + incr_curve, y0 + curveWidth + incr, x0 + straightLength, y0 + curveWidth + incr);
    path.lineTo(x0, y0 + curveWidth + incr);
    path.curveTo(x0 - 200 - incr_curve, y0 + curveWidth + incr, x0 - 200 - incr_curve, y0 - incr, x0, y0 - incr);
    path.closePath();
  }

  @Override
  void setFinishLinePoint() {
    finishLinePoint.x = x0 + 3 * straightLength / 4;
    finishLinePoint.y = y0 - WIDTH / 2;
  }
}

// abstract public class Track {
// int width;

// abstract void setPath(double x0, double y0);

// }

// class OvalTrack extends Track {
// private int straightLength = 800, curveWidth = 400;
// public Path2D path = new Path2D.Double();
// public Path2D innerPath, outerPath;
// public Rectangle2D pathBoundary;

// public OvalTrack(int width, double x0, double y0) {
// super.width = width;
// setPath(x0, y0);
// // setPath(innerPath, x0, y0, -width / 2);
// // setPath(outerPath, x0, y0, width / 2);
// pathBoundary = path.getBounds2D();
// innerPath = new Path2D.Double(path, getCenteredScaleInstance(0.8));
// outerPath = new Path2D.Double(path, getCenteredScaleInstance(1.2));
// }

// @Override
// void setPath(double x0, double y0) {
// path.moveTo(x0, y0);
// path.lineTo(x0 + straightLength, y0);
// path.curveTo(x0 + straightLength + 200, y0, x0 + straightLength + 200, y0 + curveWidth, x0 + straightLength, y0 + curveWidth);
// path.lineTo(x0, y0 + curveWidth);
// path.curveTo(x0 - 200, y0 + curveWidth, x0 - 200, y0, x0, y0);
// path.closePath();
// }

// // @Override
// // void setPath(Path2D path, double x0, double y0, double incr) {
// // path.moveTo(x0, y0 - incr);
// // path.lineTo(x0 + straightLength, y0 - incr);
// // path.curveTo(x0 + straightLength + 200 + incr, y0 - incr, x0 + straightLength + 200 + incr, y0 + curveWidth + incr, x0 + straightLength, y0 + curveWidth + incr);
// // path.lineTo(x0, y0 + curveWidth + incr);
// // path.curveTo(x0 - 200 - incr, y0 + curveWidth + incr, x0 - 200 - incr, y0 - incr, x0, y0 - incr);
// // path.closePath();
// // }

// AffineTransform getCenteredScaleInstance(double scale) {
// AffineTransform at = new AffineTransform();
// // Remember to compose functions from the right. Eg: f1 o f2 o f3
// at.translate(pathBoundary.getCenterX(), pathBoundary.getCenterY());
// at.scale(scale, scale);
// at.translate(-pathBoundary.getCenterX(), -pathBoundary.getCenterY());
// return at;
// }
// }

// abstract public class Track {
// int width;

// abstract void setPath(double x0, double y0);

// }

// class OvalTrack extends Track {
// private int straightLength = 400, curveWidth = 300;
// public Path2D.Double path = new Path2D.Double();
// public Path2D innerPath, outerPath;
// public Rectangle2D pathBoundary;

// public OvalTrack(int width, double x0, double y0) {
// super.width = width;
// setPath(x0, y0);
// // setPath(innerPath, x0, y0, -width / 2);
// // setPath(outerPath, x0, y0, width / 2);
// pathBoundary = path.getBounds2D();
// innerPath = getBoundaryPath(true);
// outerPath = getBoundaryPath(false);
// }

// @Override
// void setPath(double x0, double y0) {
// path.moveTo(x0, y0);
// path.lineTo(x0 + straightLength, y0);
// path.curveTo(x0 + straightLength + 200, y0, x0 + straightLength + 200, y0 + curveWidth, x0 + straightLength, y0 + curveWidth);
// path.lineTo(x0, y0 + curveWidth);
// path.curveTo(x0 - 200, y0 + curveWidth, x0 - 200, y0, x0, y0);
// path.closePath();
// }

// Path2D getBoundaryPath(boolean isInner) {
// double incr;
// double lastNewCoordX0 = 0, lastNewCoordY0 = 0, auxX1, auxX2, auxY1, auxY2;
// Point2D.Double p0 = new Point2D.Double();
// if (isInner)
// incr = -width / 2;
// else
// incr = width / 2;
// double EPS = incr / 5;
// double incrCurves = incr + EPS;
// double[] coords = new double[6];
// PathIterator pathIterator = path.getPathIterator(new AffineTransform());
// Path2D.Double newPath = new Path2D.Double();
// while (!pathIterator.isDone()) {
// switch (pathIterator.currentSegment(coords)) {
// case PathIterator.SEG_MOVETO:
// System.out.println(0 + "--------");
// newPath.moveTo(coords[0], coords[1] - incr);
// break;
// case PathIterator.SEG_LINETO: // Bézier curve of order 1
// System.out.println(1 + "--------");
// lastNewCoordX0 = -p0.x + coords[0] + lastNewCoordX0;
// lastNewCoordY0 = -p0.y + coords[1] + lastNewCoordY0;
// newPath.lineTo(coords[0], lastNewCoordY0);
// p0.x = coords[0];
// p0.y = coords[1];
// break;
// case PathIterator.SEG_QUADTO: // Bézier curve of order 2
// System.out.println(2 + "--------");
// auxX1 = -p0.x + coords[0] + lastNewCoordX0;
// auxY1 = -p0.y + coords[1] + lastNewCoordY0;
// lastNewCoordX0 = -p0.x + coords[2] + lastNewCoordX0;
// lastNewCoordY0 = -p0.y + coords[3] + lastNewCoordY0;
// newPath.quadTo(auxX1, auxY1, lastNewCoordX0, lastNewCoordY0);
// p0.x = coords[2];
// p0.y = coords[3];
// break;
// case PathIterator.SEG_CUBICTO: // Bézier curve of order 3
// System.out.println(3 + "--------");
// auxX1 = -p0.x + coords[0] + lastNewCoordX0;
// auxY1 = -p0.y + coords[1] + lastNewCoordY0;
// auxX2 = -p0.x + coords[2] + lastNewCoordX0;
// auxY2 = -p0.y + coords[3] + lastNewCoordY0;
// lastNewCoordX0 = -p0.x + coords[4] + lastNewCoordX0;
// lastNewCoordY0 = -p0.y + coords[5] + lastNewCoordY0;
// newPath.curveTo(auxX1, auxY1, auxX2, auxY2, lastNewCoordX0, lastNewCoordY0);
// p0.x = coords[4];
// p0.y = coords[5];
// break;
// case PathIterator.SEG_CLOSE:
// System.out.println(4);
// newPath.closePath();
// break;
// }
// pathIterator.next();
// }
// return newPath;
// }

// Path2D getBoundaryPath(boolean isInner) {
// int incr;
// double lastNewCoordX0 = 0, lastNewCoordY0 = 0, auxX1, auxX2, auxY1, auxY2;
// Point2D.Double p0 = new Point2D.Double();
// if (isInner)
// incr = -width / 2;
// else
// incr = width / 2;
// double[] coords = new double[6];
// PathIterator pathIterator = path.getPathIterator(new AffineTransform());
// Path2D.Double newPath = new Path2D.Double();
// while (!pathIterator.isDone()) {
// switch (pathIterator.currentSegment(coords)) {
// case PathIterator.SEG_MOVETO:
// System.out.println(0 + "--------");
// lastNewCoordX0 = coords[0];
// lastNewCoordY0 = coords[1] - incr;
// newPath.moveTo(lastNewCoordX0, lastNewCoordY0);
// p0.x = coords[0];
// p0.y = coords[1];
// break;
// case PathIterator.SEG_LINETO: // Bézier curve of order 1
// System.out.println(1 + "--------");
// lastNewCoordX0 = -p0.x + coords[0] + lastNewCoordX0;
// lastNewCoordY0 = -p0.y + coords[1] + lastNewCoordY0;
// newPath.lineTo(lastNewCoordX0, lastNewCoordY0);
// p0.x = coords[0];
// p0.y = coords[1];
// break;
// case PathIterator.SEG_QUADTO: // Bézier curve of order 2
// System.out.println(2 + "--------");
// auxX1 = -p0.x + coords[0] + lastNewCoordX0;
// auxY1 = -p0.y + coords[1] + lastNewCoordY0;
// lastNewCoordX0 = -p0.x + coords[2] + lastNewCoordX0;
// lastNewCoordY0 = -p0.y + coords[3] + lastNewCoordY0;
// newPath.quadTo(auxX1, auxY1, lastNewCoordX0, lastNewCoordY0);
// p0.x = coords[2];
// p0.y = coords[3];
// break;
// case PathIterator.SEG_CUBICTO: // Bézier curve of order 3
// System.out.println(3 + "--------");
// // System.out.println(p0.x);
// // System.out.println(p0.y);
// // System.out.println("oo");
// // for (int i = 0; i < coords.length; i++)
// // System.out.println(coords[i]);
// // System.out.println("oo");
// // System.out.println(lastNewCoordX0);
// // System.out.println(lastNewCoordY0);
// auxX1 = -p0.x + coords[0] + lastNewCoordX0;
// auxY1 = -p0.y + coords[1] + lastNewCoordY0;
// auxX2 = -p0.x + coords[2] + lastNewCoordX0;
// auxY2 = -p0.y + coords[3] + lastNewCoordY0;
// lastNewCoordX0 = -p0.x + coords[4] + lastNewCoordX0;
// lastNewCoordY0 = -p0.y + coords[5] + lastNewCoordY0;
// newPath.curveTo(auxX1, auxY1, auxX2, auxY2, lastNewCoordX0, lastNewCoordY0);
// p0.x = coords[4];
// p0.y = coords[5];
// break;
// case PathIterator.SEG_CLOSE:
// System.out.println(4);
// newPath.closePath();
// break;
// }
// pathIterator.next();
// }
// return newPath;
// }

// AffineTransform getCenteredScaleInstance(double scale) {
// AffineTransform at = new AffineTransform();
// // Remember to compose functions from the right. Eg: f1 o f2 o f3
// at.translate(pathBoundary.getCenterX(), pathBoundary.getCenterY());
// at.scale(scale, scale);
// at.translate(-pathBoundary.getCenterX(), -pathBoundary.getCenterY());
// return at;
// }
// }
