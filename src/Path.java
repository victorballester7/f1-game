import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public class Path {
  public Vector2D[] oldPoints = new Vector2D[4]; // old Bézier curve points.
  public Vector2D[] newPoints = new Vector2D[4]; // new Bézier curve points.
  public Vector2D[] oldPointsInner = new Vector2D[4]; // old Bézier curve points.
  public Vector2D[] newPointsInner = new Vector2D[4]; // new Bézier curve points.
  public Vector2D[] oldPointsOuter = new Vector2D[4]; // old Bézier curve points.
  public Vector2D[] newPointsOuter = new Vector2D[4]; // new Bézier curve points.
  public static double step = 0.001; // Step between points for the disctretization
  public int numSegments = 0;
  public double x0, y0; // P0 of the starting segment
  public double incrX0, incrY0;
  public boolean previousLine;
  public Vector2D incrCurve;
  public Path2D.Double path = new Path2D.Double();
  public Path2D.Double innerPath = new Path2D.Double();
  public Path2D.Double outerPath = new Path2D.Double();
  public ArrayList<Vector2D> joiningPoints = new ArrayList<Vector2D>(); // points of the path
  public ArrayList<Vector2D> discretePath = new ArrayList<Vector2D>(); // points of the path
  public ArrayList<Vector2D> diffDiscretePath = new ArrayList<Vector2D>(); // derivative of the points of the path
  public ArrayList<Vector2D> diff2DiscretePath = new ArrayList<Vector2D>(); // second derivative of the points of the path
  public Area area;

  public Path(double x0, double y0, double incrX0, double incrY0) {
    this.x0 = x0;
    this.y0 = y0;
    this.incrX0 = incrX0;
    this.incrY0 = incrY0;
    path.moveTo(x0, y0);
    innerPath.moveTo(x0 - incrX0, y0 - incrY0);
    outerPath.moveTo(x0 + incrX0, y0 + incrY0);
    joiningPoints.add(new Vector2D(x0, y0));
  }

  public void addCurve(Vector2D p, Vector2D diff, double diffForce, Vector2D incr, boolean isCurved) {
    joiningPoints.add(new Vector2D(p.x, p.y));
    if (numSegments == 0) {
      oldPoints[0] = new Vector2D(x0, y0);
      oldPoints[1] = p.clone();
      oldPoints[2] = oldPoints[0].clone();
      oldPoints[3] = p.clone();
      for (int i = 0; i < 4; i++) {
        oldPointsInner[i] = Vector2D.substract(oldPoints[i], incr);
        oldPointsOuter[i] = Vector2D.sum(oldPoints[i], incr);
        System.out.println(oldPoints[i]);
      }
      System.arraycopy(oldPoints, 0, newPoints, 0, 4);
      System.arraycopy(oldPointsInner, 0, newPointsInner, 0, 4);
      System.arraycopy(oldPointsOuter, 0, newPointsOuter, 0, 4);
    } else {
      if (isCurved) {
        incrCurve = incr.scalarProd(3);
      } else {
        incrCurve = new Vector2D();
      }
      updatePoints(p, diff, diffForce, oldPoints, newPoints, isCurved, new Vector2D(0, 0));
      updatePoints(Vector2D.substract(p, incr), diff, diffForce, oldPointsInner, newPointsInner, isCurved, incrCurve.scalarProd(-1));
      updatePoints(Vector2D.sum(p, incr), diff, diffForce, oldPointsOuter, newPointsOuter, isCurved, incrCurve);
      // System.out.println("Estem apuntant a (old-fora) " + p);
      // for (Vector2D q : oldPoints)
      // System.out.println(q);
    }
    if (isCurved) {
      path.curveTo(newPoints[1].x, newPoints[1].y, newPoints[2].x, newPoints[2].y, newPoints[3].x, newPoints[3].y);
      innerPath.curveTo(newPointsInner[1].x, newPointsInner[1].y, newPointsInner[2].x, newPointsInner[2].y, newPointsInner[3].x, newPointsInner[3].y);
      outerPath.curveTo(newPointsOuter[1].x, newPointsOuter[1].y, newPointsOuter[2].x, newPointsOuter[2].y, newPointsOuter[3].x, newPointsOuter[3].y);
      previousLine = false;
    } else {
      path.lineTo(newPoints[1].x, newPoints[1].y);
      innerPath.lineTo(newPointsInner[1].x, newPointsInner[1].y);
      outerPath.lineTo(newPointsOuter[1].x, newPointsOuter[1].y);
      previousLine = true;
    }
    disctretization();
    numSegments++;
  }

  public void finishPath() {
    for (Vector2D v : joiningPoints) {
      System.out.println(v);
    }
    path.closePath();
    innerPath.closePath();
    outerPath.closePath();
    area = new Area(outerPath);
    area.subtract(new Area(innerPath));
  }

  public void disctretization() {
    if (previousLine) {
      for (double t = 0; t < 1; t += step) {
        discretePath.add(bezier1(newPoints, t));
        diffDiscretePath.add(bezier1Diff(newPoints, t));
        diff2DiscretePath.add(new Vector2D());
      }
    } else {
      for (double t = 0; t < 1; t += step) {
        discretePath.add(bezier3(newPoints, t));
        diffDiscretePath.add(bezier3Diff(newPoints, t));
        diff2DiscretePath.add(bezier3Diff2(newPoints, t));
      }
    }

  }

  public Vector2D bezier1(Vector2D[] points, double t) {
    double x = (1 - t) * points[0].x + t * points[1].x;
    double y = (1 - t) * points[0].y + t * points[1].y;
    return new Vector2D(x, y);
  }

  public Vector2D bezier1Diff(Vector2D[] points, double t) {
    double x = points[1].x - points[0].x;
    double y = points[1].y - points[0].y;
    return new Vector2D(x, y);
  }

  public Vector2D bezier3(Vector2D[] points, double t) {
    double x = (1 - t) * (1 - t) * (1 - t) * points[0].x + 3 * t * (1 - t) * (1 - t) * points[1].x + 3 * t * t * (1 - t) * points[2].x + t * t * t * points[3].x;
    double y = (1 - t) * (1 - t) * (1 - t) * points[0].y + 3 * t * (1 - t) * (1 - t) * points[1].y + 3 * t * t * (1 - t) * points[2].y + t * t * t * points[3].y;
    return new Vector2D(x, y);
  }

  public Vector2D bezier3Diff(Vector2D[] points, double t) {
    double x = 3 * (1 - t) * (1 - t) * (points[1].x - points[0].x) + 6 * (1 - t) * t * (points[2].x - points[1].x) + 3 * t * t * (points[3].x - points[2].x);
    double y = 3 * (1 - t) * (1 - t) * (points[1].y - points[0].y) + 6 * (1 - t) * t * (points[2].y - points[1].y) + 3 * t * t * (points[3].y - points[2].y);
    return new Vector2D(x, y);
  }

  public Vector2D bezier3Diff2(Vector2D[] points, double t) {
    double x = 6 * (1 - t) * (points[2].x - 2 * points[1].x + points[0].x) + 6 * t * (points[3].x - 2 * points[2].x + points[1].x);
    double y = 6 * (1 - t) * (points[2].y - 2 * points[1].y + points[0].y) + 6 * t * (points[3].y - 2 * points[2].y + points[1].y);
    return new Vector2D(x, y);
  }

  // We want the joining points to be of class C^1. The Pi's are the points of the Bézier curve.
  // for a cubic curve: derivative at start: 3 * (P1 - P0)
  // for a cubic curve: derivative2 at start: 6 * (P2 - 2 * P1 + P0)
  // for a cubic curve: derivative at ending: 3 * (P3 - P2)
  // for a cubic curve: derivative2 at ending: 6 * (P3 - 2 * P2 + P1)
  public void updatePoints(Vector2D p, Vector2D diff, double diffForce, Vector2D[] oldPoints, Vector2D[] newPoints, boolean isCurved, Vector2D incrCurved) {
    double scale = 1. * (previousLine ? 1 : 3);
    if (newPoints == this.newPoints) {
      System.out.println("Estem apuntant a (old) " + p);
      for (Vector2D q : oldPoints)
        System.out.println(q);
      System.out.println(previousLine);
    }
    if (isCurved) {
      newPoints[0] = oldPoints[3].clone();
      Vector2D diff0 = Vector2D.sum(oldPoints[3], oldPoints[2].scalarProd(-1));
      // newPoints[1] = Vector2D.sum(newPoints[0], diff0, incrCurved.scalarProd(1. / 3));
      // newPoints[2].set(Vector2D.sum(p, diff.scalarProd(-1. / 3), incrCurved.scalarProd(-1. / 3)));
      newPoints[1] = Vector2D.sum(newPoints[0], diff0.scalarProd(scale / 3 * (diffForce / diff0.norm())), incrCurved.scalarProd(1. / 3));
      newPoints[2].set(Vector2D.sum(p, diff.scalarProd(-1. / 3 * diffForce)));
      newPoints[3].set(p);
    } else {
      newPoints[0] = oldPoints[3].clone();
      newPoints[1] = p.clone();
      newPoints[2] = newPoints[0].clone();
      newPoints[3] = newPoints[1].clone();
    }
    System.arraycopy(newPoints, 0, oldPoints, 0, 4);
    if (newPoints == this.newPoints) {
      // System.out.println("Estem apuntant a (old2) " + p);
      // for (Vector2D q : oldPoints)
      // System.out.println(q);
      System.out.println("Estem apuntant a (new) " + p);
      for (Vector2D q : newPoints)
        System.out.println(q);
    }
  }
}
