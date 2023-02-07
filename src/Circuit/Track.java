package Circuit;

import java.awt.*;
import java.awt.geom.*;

import Cars.Car;
import Main.Game;
import Main.Window;
import Misc.Vector2D;

abstract public class Track {
  // we assume by the moment that all tracks start in a straight line with a long
  // straight pointing to the right --->.
  public double widthTrack;
  // customizations
  public Vector2D finishLinePoint = new Vector2D();
  public Area blackSquaresfinishLine = new Area();
  public Area whiteSquaresfinishLine = new Area();
  public Area areaSquaresfinishLine = new Area();
  public Path2D grid = new Path2D.Double();;
  public static double widthFinishLine = 4;
  public static double marginGridSlot = 0.25;
  public static double widthGridSlot = Car.width + 2 * marginGridSlot;
  public static double lengthGridSlot = Car.length / 2;
  public int numPaths;
  public Path[] paths;
  public Area area;
  public Path[] pathsBestTrajectory;
  public static double marginWithWeed;
  public Rectangle2D bounds;
  public Color trackColor = new Color(100, 100, 100);
  public Vector2D startDirection;
  protected double x0, y0;

  public Track(double widthTrack, double x0, double y0, int numPaths, Vector2D startDirection) {
    this.widthTrack = widthTrack;
    marginWithWeed = 2 * widthTrack;
    this.x0 = x0;
    this.y0 = y0;
    this.numPaths = numPaths;
    this.startDirection = startDirection;
    paths = setPath();
    pathsBestTrajectory = setBestPath();
    area = joinAreas(paths);
    bounds = setScaledMargins(area.getBounds2D());
    setFinishLinePoint();
    createGrid();
    areaSquaresfinishLine.add(blackSquaresfinishLine);
    areaSquaresfinishLine.add(whiteSquaresfinishLine);
  }

  public Area joinAreas(Path[] p) {
    Area newArea = new Area();
    for (Path pi : p)
      newArea.add(pi.area);
    return newArea;
  }

  abstract public Path[] setPath();

  abstract public Path[] setBestPath();

  abstract void setFinishLinePoint();

  private Rectangle2D setScaledMargins(Rectangle2D rect) {
    Rectangle2D newRect = new Rectangle2D.Double(rect.getX() - marginWithWeed, rect.getY() - marginWithWeed, rect.getWidth() + 2 * marginWithWeed, rect.getHeight() + 2 * marginWithWeed);

    // correct the aspect ratio
    double ratio = newRect.getWidth() / newRect.getHeight();
    double newLength, newCoordinate;
    if (ratio >= Window.screenRatio) { // circuit is too wide
      newLength = newRect.getWidth() / Window.screenRatio;
      newCoordinate = newRect.getY();
      newCoordinate = newRect.getY() - (newLength - newRect.getHeight()) / 2;
      newRect.setRect(newRect.getX(), newCoordinate, newRect.getWidth(), newLength);
    } else { // circuit is too high
      newLength = Window.screenRatio * newRect.getHeight();
      newCoordinate = newRect.getX() - (newLength - newRect.getWidth()) / 2;
      newRect.setRect(newCoordinate, newRect.getY(), newLength, newRect.getHeight());
    }

    return newRect;
  }

  private void createFinishLine() {
    int verticalNumSquares = 21;
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
  // paintCarGridSlot(new Point2D.Double(c.initialPosition.x + Car.HEIGHT / 2,
  // c.initialPosition.y - Car.WIDTH / 2), g2);
  // }
  // }

  private void createGrid() {
    createFinishLine();
    Vector2D v;
    for (int i = 1; i <= Game.numCars; i++) {
      v = Car.setInitialPosition(this, i);
      createCarGridSlot(new Vector2D(v.x + Car.length / 2, v.y + Car.width / 2));
    }
  }

  private void createCarGridSlot(Vector2D p) {
    // The point P is the right top edge of the car and th point (x0, y0) is the one
    // marked with a circle in the figure below.
    // Ignore the points in the middle (think they are invisible)
    // ---o
    // ...|
    // ...|
    // ...|
    // ...|
    // ----
    double x0 = p.x + marginGridSlot;
    double y0 = p.y + marginGridSlot;
    Path2D.Double path = new Path2D.Double();
    path.moveTo(x0 - lengthGridSlot, y0);
    path.lineTo(x0, y0);
    path.lineTo(x0, y0 - widthGridSlot);
    path.lineTo(x0 - lengthGridSlot, y0 - widthGridSlot);
    grid.append(path, false);
  }

  // get the tangent vector of a cubic Bézier curve.
  // public Vector2D getTangentVector(Vector2D p, double[] curve, boolean ...
  // isLine){

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
    // g2.fill(at.createTransformedShape(areaBestTrajectory));
    for (Path p : pathsBestTrajectory) {
      if (p instanceof Line)
        g2.draw(at.createTransformedShape(((Line) p).line));
      else
        g2.draw(at.createTransformedShape(((Curve) p).curve));
    }
    // g2.draw(at.createTransformedShape(path.path));
    // g2.setColor(Color.BLUE);
    // g2.draw(at.createTransformedShape(bestTrajectory.innerPath));
    // g2.draw(at.createTransformedShape(path.innerPath));
    // g2.fill(at.createTransformedShape(new Ellipse2D.Double(bounds.getCenterX(),
    // bounds.getCenterY(), 20, 20)));
    // g2.setColor(Color.PINK);
    // g2.fill(at.createTransformedShape(paths[3].area));
    // g2.draw(at.createTransformedShape(bestTrajectory.outerPath));
    // g2.draw(at.createTransformedShape(path.outerPath));

    // g2.setColor(Color.RED);
    // for (Vector2D v : path.joiningPoints) {
    // g2.fill(at.createTransformedShape(new Ellipse2D.Double(v.x, v.y, 20, 20)));
    // }
  }
}