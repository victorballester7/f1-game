import java.awt.*;
import java.awt.event.*;
import java.awt.Graphics;
import java.awt.geom.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.AttributeSet.ColorAttribute;
import java.util.Timer;
import java.util.*;

import java.io.*;

public class Game {
  int WIDTH = 1500, HEIGHT = 1000;
  // Graphics g;
  private Image backImg = null;
  private JFrame frame;
  private gamePanel gPanel;
  public static final int NumCars = 6;
  private Track t;
  public static Car[] cars = new Car[NumCars];
  private int DELAY = 100;

  public Game() {
    initialize();
    new MainLoop();
    // new Timer().schedule(new MainLoop(), 100, DELAY);
  }

  private void initialize() {
    frame = new JFrame();
    frame.setTitle("F1 game"); // Name of the window
    frame.setSize(WIDTH, HEIGHT);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // use x button to close the window
    frame.setLocationRelativeTo(null); // centers the window on the screen
    frame.setResizable(false); // prevent the window to be resizable

    t = new OvalTrack(100, 300, 300);

    cars[0] = new Mercedes(1, true);
    cars[1] = new Ferrari(2, false);
    cars[2] = new RedBull(3, false);
    cars[3] = new Mercedes(4, false);
    cars[4] = new Ferrari(5, false);
    cars[5] = new RedBull(6, false);

    try {
      backImg = ImageIO.read(new File("images/grass.jpg"));
      backImg = backImg.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);
    } catch (IOException e) {
      e.printStackTrace();
    }
    gPanel = new gamePanel();
    frame.setContentPane(gPanel);
  }

  public void show() {
    frame.setVisible(true); // activates the visibility
  }

  public void update() {
    for (Car c : cars) {// Cars
      c.update();
    }
    // gPanel.repaint();
  }

  class gamePanel extends JPanel {
    // private Image backImg;

    // public TrackPanel(Image img) {
    // this.img = img;
    // }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g.drawImage(backImg, 0, 0, this); // background image
      t.drawTrack(g2); // Track

      for (Car c : cars) {// Cars
        c.drawCar(g2);
      }
    }
  }

  private class MainLoop extends TimerTask {
    @Override
    public void run() {
      update();
      gPanel.repaint();
    }
  }
  // public void update(Graphics g) {
  // paint(g);
  // }
}
