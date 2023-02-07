package Circuit;

import java.awt.geom.*;
import Misc.Vector2D;

interface RelativePosition {
  // public Shape getPath(Vector2D p);

  public double getClosestPoint(Vector2D p);

  public Vector2D position(double t);

  public Vector2D tangent(double t);

  public Vector2D normal(double t);
}

public class Path {
  public Vector2D startPoint;
  public Vector2D endPoint;
  public Vector2D startPointInner;
  public Vector2D endPointInner;
  public Vector2D startPointOuter;
  public Vector2D endPointOuter;
  public boolean dimension2; // true for tracks, false for 1 dimensional curves.
  public double width;
  public Path2D.Double boundary;
  public Area area;

  public Path(Vector2D startPoint, Vector2D endPoint, Vector2D startPointInner, Vector2D endPointInner, Vector2D startPointOuter, Vector2D endPointOuter) {
    this.startPoint = startPoint.clone();
    this.endPoint = endPoint.clone();
    this.startPointInner = startPointInner.clone();
    this.endPointInner = endPointInner.clone();
    this.startPointOuter = startPointOuter.clone();
    this.endPointOuter = endPointOuter.clone();
    dimension2 = true;
  }

  public Path(Vector2D startPoint, Vector2D endPoint, double width) {
    this.startPoint = startPoint.clone();
    this.endPoint = endPoint.clone();
    this.width = width;
    if (width < 0.001)
      dimension2 = false;
    else
      dimension2 = true;
  }

  public Path(Vector2D startPoint, Vector2D endPoint) {
    this.startPoint = startPoint.clone();
    this.endPoint = endPoint.clone();
    dimension2 = false;
  }

  public void setWidth(double width) {
    this.width = width;
  }

  public double getClosestPoint(Vector2D p) {
    return 0;
  }

  public Vector2D positionTo(Vector2D p, double t) {
    return p.substract(position(t));
  }

  public Vector2D position(double t) {
    return null;
  }

  public Vector2D tangent(double t) {
    return null;
  }

  public Vector2D normal(double t) {
    return null;
  }
}