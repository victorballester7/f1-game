import java.awt.geom.*;

public class Vector2D {
  double x, y;

  public Vector2D(double x, double y) {
    set(x, y);
  }

  public Vector2D() {
    set(0, 0);
  }

  public double distance(Vector2D p) {
    return Math.sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y));
  }

  public double norm() {
    return distance(new Vector2D());
  }

  public Vector2D sum(Vector2D v) {
    return new Vector2D(x + v.x, y + v.y);
  }

  public static Vector2D sum(Vector2D... vectors) { // arbitrary number of vectors
    Vector2D result = vectors[0];
    for (int i = 1; i < vectors.length; i++)
      result = result.sum(vectors[i]);
    return result;
  }

  public static Vector2D[] sum(Vector2D[]... arrays) { // arbitrary number of arrays
    // array1, array2, array3,... are supposed to have the same length
    Vector2D[] result = new Vector2D[arrays[0].length];
    for (int i = 0; i < arrays[0].length; i++)
      result[i] = new Vector2D();
    for (Vector2D[] array : arrays) {
      for (int i = 0; i < array.length; i++)
        result[i] = result[i].sum(array[i]);
    }
    return result;
  }

  public Vector2D scalarProd(double a) {
    return new Vector2D(x * a, y * a);
  }

  public static Vector2D[] scalarProd(Vector2D[] array, double a) {
    Vector2D[] result = new Vector2D[array.length];
    for (int i = 0; i < array.length; i++)
      result[i] = array[i].scalarProd(a);
    return result;
  }

  public double dotProduct(Vector2D v) {
    return x * v.x + y * v.y;
  }

  public double det(Vector2D v) { // determinant of [u, v]
    return x * v.y - y * v.x;
  }

  public static double signedAngle(Vector2D u, Vector2D v) {
    return Math.signum(u.det(v)) * Math.acos(u.dotProduct(v) / (u.norm() * v.norm()));
  }

  // public void translate(Vector2D v) {
  // x += v.x;
  // y += v.y;
  // }

  public Vector2D rotate(double angle) {
    double new_x = Math.cos(angle) * x - Math.sin(angle) * y;
    double new_y = Math.sin(angle) * x + Math.cos(angle) * y;
    return new Vector2D(new_x, new_y);
  }

  public Vector2D rotate(double angle, Vector2D v) {
    double new_x = v.x + Math.cos(angle) * (x - v.x) - Math.sin(angle) * (y - v.y);
    double new_y = v.y + Math.sin(angle) * (x - v.x) + Math.cos(angle) * (y - v.y);
    return new Vector2D(new_x, new_y);
  }

  public String print() {
    return "(" + x + ", " + y + ")";
  }

  public void set(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public void set(Vector2D newVect) {
    this.x = newVect.x;
    this.y = newVect.y;
  }

  public Point2D.Double toPoint() {
    return new Point2D.Double(x, y);
  }

  public static Vector2D toVector(Point2D p) {
    return new Vector2D(p.getX(), p.getY());
  }
}