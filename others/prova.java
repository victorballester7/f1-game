import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.*;

public class prova {
  public static void main(String[] args) throws IOException {
    JFrame frame = new JFrame("Test");

    frame.add(new JComponent() {
      BufferedImage image = ImageIO.read(new File("images/cars/carRedBull.png"));

      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // create the transform, note that the transformations happen
        // in reversed order (so check them backwards)
        AffineTransform at = new AffineTransform();

        // 4. translate it to the center of the component
        at.translate(image.getWidth() / 2, image.getHeight() / 2);

        // 3. do the actual rotation
        at.rotate(0);

        // 2. just a scale because this image is big
        at.scale(0.5, 0.5);

        // 1. translate the object so that you rotate it around the
        // center (easier :))
        at.translate(-image.getWidth() / 2, -image.getHeight() / 2);

        // draw the image
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(image, at, null);

        // continue drawing other stuff (non-transformed)
        // ...
      }
    });

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(400, 400);
    frame.setVisible(true);
  }
}
