import java.awt.geom.*;

public class Vector2D {
  double x, y;

  public Vector2D(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public Vector2D() {
    this.x = 0;
    this.y = 0;
  }

  double distance(Vector2D p) {
    return Math.sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y));
  }

  double norm() {
    return distance(new Vector2D());
  }

  // Vector2D rotate(double angle) {
  // double new_x = (double) Math.cos(angle) * x - Math.sin(angle) * y;
  // double new_y = (double) Math.sin(angle) * x + Math.cos(angle) * y;
  // return new Vector2D(new_x, new_y);
  // }
  void rotate(double angle, Point2D.Double p) {
    double new_x = p.x + Math.cos(angle) * (x - p.x) - Math.sin(angle) * (y - p.y);
    double new_y = p.y + Math.sin(angle) * (x - p.x) + Math.cos(angle) * (y - p.y);
    x = new_x;
    y = new_y;
  }

}
