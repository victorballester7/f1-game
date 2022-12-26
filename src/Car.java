import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.text.Position;

abstract public class Car {
  public static final int WIDTH_ORIGINAL = 38;
  public static final int HEIGHT_ORIGINAL = 2 * WIDTH_ORIGINAL;
  public static final int WIDTH = 20;
  public static final int HEIGHT = 2 * WIDTH;
  public static final double scale = WIDTH * 1. / WIDTH_ORIGINAL;
  public int orderedPosition; // 1st, 2nd, ...
  public double rotationAngle = Math.toRadians(0);
  public Vector2D position = new Vector2D();
  public Vector2D direction = new Vector2D(1, 0);
  public Vector2D velocity = new Vector2D();
  public BufferedImage img;
  private boolean isPlayer;

  public Car(int orderedPosition, boolean isPlayer) {
    this.orderedPosition = orderedPosition;
    this.isPlayer = isPlayer;
    img = setCar();
    setInitialPosition();
  }

  private BufferedImage setCar() {
    BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
    ;
    String fileName = getCarImagePath();
    try {
      img = ImageIO.read(new File(fileName));
      // img = (BufferedImage) img.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);
    } catch (IOException e) {
      img = null;
      // e.printStackTrace();
    }
    return img;
  }

  public void setInitialPosition() {
    int sepFromLine = 10;
    if (orderedPosition % 2 == 1) { // 1st, 3rd, 5th.. cars
      position.x = Track.finishLinePoint.x - sepFromLine - HEIGHT / 2 - (orderedPosition - 1) * HEIGHT;
      position.y = Track.finishLinePoint.y + Track.WIDTH / 4;
      // position.y = Track.finishLinePoint.y + W (WIDTH / 2 - Track.widthGridSlot) / 2;
    } else {// 2nd, 4th, 6th.. cars
      position.x = Track.finishLinePoint.x - sepFromLine - HEIGHT / 2 - (orderedPosition - 1) * HEIGHT;
      position.y = Track.finishLinePoint.y + 3 * Track.WIDTH / 4;
      // position.y = Track.finishLinePoint.y + WIDTH / 2 + (WIDTH / 2 - Track.widthGridSlot) / 2;
    }
  }

  abstract String getCarImagePath();

  public void update() {
    rotationAngle = Math.signum(direction.x) * Math.acos(direction.y / direction.norm()); // dot product
    // rotationAngle += Math.toRadians(20);
    // position.rotate(rotationAngle);

  }

  // public double getRotationAngle(){
  // return -Math.signum(direction.x) * Math.acos(direction.y / direction.norm());
  // }

  public void drawCar(Graphics2D g2) {
    if (img == null) {
      g2.setColor(Color.BLACK);
      Path2D.Double path = drawRotatedRectangle(position.x - WIDTH / 2, position.y - HEIGHT / 2, WIDTH, HEIGHT, rotationAngle);
      g2.fill(path); // oriented towards the right. (-->)
    } else {
      BufferedImage rotImage = rotateAndScaleImage(img, rotationAngle);
      // double x0 = position.x - rotImage.getWidth() / 2;
      // double y0 = position.y - rotImage.getHeight() / 2;
      // Vector2D v0 = new Vector2D(x0, y0);

      // g2.setColor(Color.WHITE);
      // g2.draw(new Rectangle2D.Double(x0, y0, rotImage.getWidth(), rotImage.getHeight()));
      // v0.rotate(rotationAngle, new Point2D.Double(position.x, position.y));

      // g2.setColor(Color.ORANGE);
      // g2.fill(new Ellipse2D.Double(Track.finishLinePoint.x, Track.finishLinePoint.y, 5, 5));
      // g2.setColor(Color.RED);

      // g2.draw(drawRotatedRectangle(x0, y0, rotImage.getWidth(), rotImage.getHeight(), rotationAngle));
      g2.drawImage(rotImage, (int) Math.round(position.x - rotImage.getWidth() / 2), (int) Math.round(position.y - rotImage.getHeight() / 2), null);
    }
  }

  public Path2D.Double drawRotatedRectangle(double x0, double y0, double width, double height, double angle) {

    Vector2D[] v = new Vector2D[4];
    Point2D.Double p = new Point2D.Double(x0 + width / 2, y0 + height / 2);
    v[0] = new Vector2D(x0, y0);
    v[1] = new Vector2D(x0 + width, y0);
    v[2] = new Vector2D(x0 + width, y0 + height);
    v[3] = new Vector2D(x0, y0 + height);

    for (Vector2D vi : v) {
      vi.rotate(angle, p);
    }

    Path2D.Double path = new Path2D.Double();
    path.moveTo(v[0].x, v[0].y);
    for (int i = 1; i < 4; i++) {
      path.lineTo(v[i].x, v[i].y);
    }
    path.closePath();
    return path;
  }

  public BufferedImage rotateAndScaleImage(BufferedImage imageToRotate, double angle) {
    // int oldWidth = imageToRotate.getWidth();
    // int oldHeight = imageToRotate.getHeight();
    int newWidth = (int) Math.round(Math.abs(Math.cos(angle)) * WIDTH + Math.abs(Math.sin(angle)) * HEIGHT);
    int newHeight = (int) Math.round(Math.abs(Math.sin(angle)) * WIDTH + Math.abs(Math.cos(angle)) * HEIGHT);

    BufferedImage newImage = new BufferedImage(newWidth, newHeight, imageToRotate.getType());

    Graphics2D g2 = newImage.createGraphics();
    g2.rotate(angle, newWidth / 2, newHeight / 2);

    g2.drawImage(imageToRotate, newWidth / 2 - WIDTH / 2, newHeight / 2 - HEIGHT / 2, newWidth / 2 + WIDTH / 2, newHeight / 2 + HEIGHT / 2, 0, 0, WIDTH_ORIGINAL, HEIGHT_ORIGINAL, null);
    // public abstract boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer)
    // img - the specified image to be drawn. This method does nothing if img is null.
    // dx1 - the x coordinate of the first corner of the destination rectangle.
    // dy1 - the y coordinate of the first corner of the destination rectangle.
    // dx2 - the x coordinate of the second corner of the destination rectangle.
    // dy2 - the y coordinate of the second corner of the destination rectangle.
    // sx1 - the x coordinate of the first corner of the source rectangle.
    // sy1 - the y coordinate of the first corner of the source rectangle.
    // sx2 - the x coordinate of the second corner of the source rectangle.
    // sy2 - the y coordinate of the second corner of the source rectangle.
    // observer - object to be notified as more of the image is scaled and converted.
    return newImage;
  }
}

class Mercedes extends Car {

  public Mercedes(int orderedPosition, boolean isPlayer) {
    super(orderedPosition, isPlayer);
  }

  @Override
  String getCarImagePath() {
    return "images/car_mercedes.png";
  }
}

class Ferrari extends Car {

  public Ferrari(int orderedPosition, boolean isPlayer) {
    super(orderedPosition, isPlayer);
  }

  @Override
  String getCarImagePath() {
    return "images/car_ferrari.png";
  }

}

class RedBull extends Car {

  public RedBull(int orderedPosition, boolean isPlayer) {
    super(orderedPosition, isPlayer);
  }

  @Override
  String getCarImagePath() {
    return "images/car_redBull.png";
  }

}