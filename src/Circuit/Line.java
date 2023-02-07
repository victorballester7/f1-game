package Circuit;

import java.awt.geom.*;

import Misc.Vector2D;

public class Line extends Path implements RelativePosition {
  public Line2D.Double line;
  public Line2D.Double lineInner;
  public Line2D.Double lineOuter;
  public Vector2D direction;
  public double length;

  public Line(Vector2D startPoint, Vector2D endPoint, double width) {
    super(startPoint, endPoint, width);
    direction = endPoint.substract(startPoint);
    length = direction.norm();
    direction = direction.normalize();
    setLine();
    createArea();
  }

  public Line(Vector2D startPoint, Vector2D endPoint) {
    super(startPoint, endPoint, 0);
    direction = endPoint.substract(startPoint);
    length = direction.norm();
    direction = direction.normalize();
    setLine();
  }

  public void createArea() {
    boundary = new Path2D.Double(lineOuter);
    boundary.lineTo(endPointInner.x, endPointInner.y);
    boundary.lineTo(startPointInner.x, startPointInner.y); // we have to reverse the innerCurve
    boundary.closePath();
    area = new Area(boundary);
  }

  public void setLine() { // we suppose that the circuits are all clockwise
    line = new Line2D.Double(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
    if (dimension2) {
      startPointInner = startPoint.sum(direction.rotate(-Math.PI / 2).scalarProd(width / 2));
      startPointOuter = startPoint.sum(direction.rotate(Math.PI / 2).scalarProd(width / 2));
      endPointInner = startPointInner.sum(direction.scalarProd(length));
      endPointOuter = startPointOuter.sum(direction.scalarProd(length));
      lineInner = new Line2D.Double(startPointInner.x, startPointInner.y, endPointInner.x, endPointInner.y);
      lineOuter = new Line2D.Double(startPointOuter.x, startPointOuter.y, endPointOuter.x, endPointOuter.y);
    }
  }

  @Override
  public double getClosestPoint(Vector2D p) { // get closest point (time t) of the line from point p.
    // solution analytically:
    double t = startPoint.substract(p).dotProduct(direction.scalarProd(-1 / length));
    return t;
  }

  @Override
  public Vector2D position(double t) { // has to be Overridden by the other methods.
    double x = startPoint.x + t * (endPoint.x - startPoint.x);
    double y = startPoint.y + t * (endPoint.y - startPoint.y);
    return new Vector2D(x, y);
  }

  @Override
  public Vector2D tangent(double t) {
    return direction.clone();
  }

  @Override
  public Vector2D normal(double t) { // normal to the right of the line.
    Vector2D n = direction.clone();
    return n.rotate(-Math.PI / 2);
  }

  // @Override
  // public Shape getPath(Vector2D p) {
  // return line;
  // }
}