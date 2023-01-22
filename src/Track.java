import java.awt.*;
import java.awt.geom.*;

abstract public class Track {
  // we assume by the moment that all tracks start in a straight line with a long straight pointing to the right --->.
  public static double widthTrack;
  // public static double width;
  // public static double height;
  public static Vector2D finishLinePoint = new Vector2D();
  public Area blackSquaresfinishLine = new Area();
  public Area whiteSquaresfinishLine = new Area();
  public Path2D grid = new Path2D.Double();;
  public static double widthFinishLine = 4;
  public static double marginGridSlot = 0.25;
  public static double widthGridSlot = Car.width + 2 * marginGridSlot;
  public static double lengthGridSlot = Car.length / 2;
  public Path2D.Double path = new Path2D.Double();
  public Path2D.Double innerPath = new Path2D.Double();
  public Path2D.Double outerPath = new Path2D.Double();
  public Path2D.Double bestTrajectory = new Path2D.Double();
  public Area area;
  private static double margin;
  public Rectangle2D bounds;
  public Color trackColor = new Color(100, 100, 100);
  public Vector2D startDirection;
  protected double x0, y0;

  public Track(double widthTrack, double x0, double y0, Vector2D startDirection) {
    Track.widthTrack = widthTrack;
    margin = 2 * widthTrack;
    this.x0 = x0;
    this.y0 = y0;
    this.startDirection = startDirection;
    setPath(path, 0);
    setPath(innerPath, -widthTrack / 2);
    setPath(outerPath, widthTrack / 2);
    setPath(bestTrajectory, widthTrack / 2);
    area = new Area(outerPath);
    area.subtract(new Area(innerPath));
    bounds = setScaledMargins(area.getBounds2D());
    setFinishLinePoint();
    createGrid();
  }

  abstract void setPath(Path2D path, double incr);

  abstract void setFinishLinePoint();

  private Rectangle2D setScaledMargins(Rectangle2D rect) {
    Rectangle2D newRect = new Rectangle2D.Double(rect.getX() - margin, rect.getY() - margin, rect.getWidth() + 2 * margin, rect.getHeight() + 2 * margin);

    // correct the aspect ratio
    double ratio = newRect.getWidth() / newRect.getHeight();
    double newLength, newCoordinate;
    if (ratio >= Window.screenRatio) { // circuit is too wide
      newLength = newRect.getWidth() / Window.screenRatio;
      newCoordinate = newRect.getY() - (newLength - newRect.getHeight()) / 2;
      newRect.setRect(new Rectangle2D.Double(newRect.getX(), newCoordinate, newRect.getWidth(), newLength));
    } else { // circuit is too high
      newLength = Window.screenRatio * newRect.getHeight();
      newCoordinate = newRect.getX() - (newLength - newRect.getWidth()) / 2;
      newRect.setRect(new Rectangle2D.Double(newCoordinate, newRect.getY(), newLength, newRect.getHeight()));
    }

    return newRect;
  }

  // private void paintFinishLine(Graphics2D g2) {
  // int verticalNumSquares = 20;
  // double sideLength = widthTrack / verticalNumSquares;
  // int horizontalNumSquares = (int) Math.round(widthFinishLine / sideLength);
  // for (int i = 0; i < horizontalNumSquares; i++) {
  // for (int j = 0; j < verticalNumSquares; j++) {
  // if ((i + j) % 2 == 1)
  // g2.setColor(Color.WHITE);
  // else
  // g2.setColor(Color.BLACK);
  // g2.fill(new Rectangle2D.Double(finishLinePoint.x + i * sideLength, finishLinePoint.y + j * sideLength, sideLength, sideLength));
  // }
  // }
  // }

  private void createFinishLine() {
    int verticalNumSquares = 20;
    double sideLength = widthTrack / verticalNumSquares;
    int horizontalNumSquares = (int) Math.round(widthFinishLine / sideLength);
    for (int i = 0; i < horizontalNumSquares; i++) {
      for (int j = 0; j < verticalNumSquares; j++) {
        if ((i + j) % 2 == 1)
          whiteSquaresfinishLine.add(new Area(new Rectangle2D.Double(finishLinePoint.x + i * sideLength, finishLinePoint.y + j * sideLength, sideLength, sideLength)));
        else
          blackSquaresfinishLine.add(new Area(new Rectangle2D.Double(finishLinePoint.x + i * sideLength, finishLinePoint.y + j * sideLength, sideLength, sideLength)));
      }
    }
  }

  // private void paintGrid(Car[] cars, Graphics2D g2) {
  // paintFinishLine(g2);
  // for (Car c : cars) {
  // paintCarGridSlot(new Point2D.Double(c.initialPosition.x + Car.HEIGHT / 2, c.initialPosition.y - Car.WIDTH / 2), g2);
  // }
  // }

  private void createGrid() {
    createFinishLine();
    Vector2D v;
    for (int i = 1; i <= Game.NumCars; i++) {
      v = Car.setInitialPosition(startDirection, i);
      createCarGridSlot(new Vector2D(v.x + Car.length / 2, v.y - Car.width / 2));
    }
  }

  private void createCarGridSlot(Vector2D p) {
    // The point P is the right top edge of the car and th point (x0, y0) is the one marked with a circle in the figure below.
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
    grid.append(path, false);
  }

  // get the tangent vector of a cubic Bézier curve.
  // public Vector2D getTangentVector(Vector2D p, double[] curve, boolean ... isLine){

  // }

  public void drawTrack(Graphics2D g2, AffineTransform at) {
    g2.setColor(trackColor);
    g2.fill(at.createTransformedShape(area));
    g2.setColor(Color.BLACK);
    g2.fill(at.createTransformedShape(blackSquaresfinishLine));
    g2.setColor(Color.WHITE);
    g2.fill(at.createTransformedShape(whiteSquaresfinishLine));
    g2.draw(at.createTransformedShape(grid));
    // g2.draw(at.createTransformedShape(bounds));
    g2.setColor(Color.YELLOW);
    g2.draw(at.createTransformedShape(bestTrajectory));
  }
}

class OvalTrack extends Track {
  private static final double straightLength = 1006, curveWidth = 400;

  public OvalTrack(double widthTrack, double x0, double y0, Vector2D startDirection) {
    super(widthTrack, x0, y0, startDirection);
  }

  @Override
  void setPath(Path2D path, double incr) {
    // in order for the joining points to be of class C^1 (smooth) we need that (P3 - P2) = (Q1 - Q2), where Pi are the points of the first Bézier curve and Qi are the points of the second Bézier curve.
    double EPS = incr / 3;
    double incr_curve = incr + EPS;
    double margin = 0;
    double finalBrakingPoint = 0;
    double curvature = straightLength / 4;
    if (path == bestTrajectory) {
      margin = widthTrack / 5;
      finalBrakingPoint = incr_curve + incr - straightLength / 150;
    }
    path.moveTo(x0 + finalBrakingPoint, y0 - incr + margin);
    path.lineTo(x0 + straightLength - finalBrakingPoint, y0 - incr + margin);
    path.curveTo(x0 + straightLength + curvature + incr_curve - finalBrakingPoint, y0 - incr + margin, x0 + straightLength + curvature + incr_curve - finalBrakingPoint, y0 + curveWidth + incr - margin, x0 + straightLength - finalBrakingPoint, y0 + curveWidth + incr - margin);
    path.lineTo(x0 + finalBrakingPoint, y0 + curveWidth + incr - margin);
    path.curveTo(x0 - curvature - incr_curve + finalBrakingPoint, y0 + curveWidth + incr - margin, x0 - curvature - incr_curve + finalBrakingPoint, y0 - incr + margin, x0 + finalBrakingPoint, y0 - incr + margin);
    path.closePath();
  }

  @Override
  void setFinishLinePoint() {
    finishLinePoint.x = x0 + 3 * straightLength / 4;
    finishLinePoint.y = y0 - widthTrack / 2;
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
