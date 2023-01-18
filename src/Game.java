import java.awt.*;
import java.awt.event.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;

import java.io.*;

public class Game implements KeyListener, ActionListener {
  public static final int WIDTH = 1920, HEIGHT = 1080;
  private Image backImg = null;
  private Image playButtonImg = null;
  private static final int playButtonImgWidth = 50;
  private boolean playButtonPressed = false;
  private Image[] lightsImg = new Image[7];
  private static final int lightsImgWidth = 300;
  private JFrame frame;
  private gamePanel gPanel;
  private JButton playButton;
  public static final int NumCars = 6;
  private Track t;
  public static Car[] cars = new Car[NumCars];
  public boolean lightsOut = false;
  private static final int lightsDelay = 1000; // in milliseconds
  public boolean keyLeft = false;
  public boolean keyRight = false;
  public boolean keyUp = false;
  public boolean keyDown = false;

  public Game() {
    initialize();
    // new MainLoop();
    // new Timer().schedule(new MainLoop(), 100, DELAY);
  }

  private void initialize() {
    frame = new JFrame();
    frame.setTitle("F1 game"); // Name of the window
    frame.setSize(WIDTH, HEIGHT);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // use x button to close the window
    frame.setResizable(false); // prevent the window to be resizable
    frame.addKeyListener(this);
    frame.setLayout(null);

    t = new OvalTrack(100, 300, 300, new Vector2D(1, 0));

    cars[0] = new Mercedes(1, t, true);
    cars[1] = new Ferrari(2, t, false);
    cars[2] = new RedBull(3, t, false);
    cars[3] = new Mercedes(4, t, false);
    cars[4] = new Ferrari(5, t, false);
    cars[5] = new RedBull(6, t, false);

    try {
      // background
      backImg = ImageIO.read(new File("images/grass.jpg"));
      backImg = backImg.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);

      // lights
      lightsImg[0] = ImageIO.read(new File("images/lights/off.png"));
      for (int i = 1; i < 6; i++) {
        lightsImg[i] = ImageIO.read(new File("images/lights/red" + i + ".png"));
      }
      lightsImg[6] = ImageIO.read(new File("images/lights/green.png"));
      double scale = lightsImgWidth * 1. / lightsImg[0].getWidth(frame);
      for (int i = 0; i < 7; i++) {
        lightsImg[i] = lightsImg[i].getScaledInstance(lightsImgWidth, (int) Math.round(lightsImg[i].getHeight(frame) * scale), Image.SCALE_DEFAULT);
      }

      // Play button
      // playButtonImg = ImageIO.read(new File());
      // scale = playButtonImgWidth * 1. / playButtonImg.getWidth(frame);
      // playButtonImg = playButtonImg.getScaledInstance(playButtonImgWidth, (int) Math.round(playButtonImg.getHeight(frame) * scale), Image.SCALE_DEFAULT);
    } catch (IOException e) {
      e.printStackTrace();
    }
    gPanel = new gamePanel();
    frame.setContentPane(gPanel);

    // Play button
    playButton = new JButton(new ImageIcon("images/misc/playButton.png"));
    // playButton.setMargin(new Insets(0, 0, 0, 0));
    // playButton.setBackground(Color.CYAN);
    // playButton.setBorder(new RoundedBorder(20));
    // playButton.setBorder(null);
    playButton.setBounds(200, 200, 500, 150);
    frame.add(playButton);
    playButton.addActionListener(this);
  }

  public void show() {
    frame.setVisible(true); // activates the visibility
  }

  public void update() {
    if (lightsOut) {
      for (Car c : cars) {// Cars
        if (c.isPlayer) {
          if (keyLeft)
            c.direction = c.direction.rotate(Math.toRadians(-c.rotationRate()));

          if (keyRight)
            c.direction = c.direction.rotate(Math.toRadians(c.rotationRate()));

          if (keyUp)
            c.appliedEngineForce = c.engineMAX;
          else
            c.appliedEngineForce = 0;

          if (keyDown)
            c.appliedBrakeForce = c.brakeMAX;
          else
            c.appliedBrakeForce = 0;
        }
        c.update();
      }
    }
  }

  @Override
  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
    case 37: // left arrow
      keyLeft = true;
      // cars[0].rotationAngle = -Car.rotationRate;
      break;
    case 38: // up arrow
      keyUp = true;
      break;
    case 39: // right arrow
      keyRight = true;
      // cars[0].rotationAngle = Car.rotationRate;
      break;
    case 40: // down arrow
      keyDown = true;
      break;
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
    switch (e.getKeyCode()) {
    case 37: // left arrow
      keyLeft = false;
      break;
    case 38: // up arrow
      keyUp = false;
      break;
    case 39: // right arrow
      keyRight = false;
      break;
    case 40: // down arrow
      keyDown = false;
      break;
    }
  }

  @Override
  public void keyTyped(KeyEvent e) {
    // TODO Auto-generated method stub

  }

  private static class RoundedBorder implements Border {
    // Reference: https://stackoverflow.com/questions/423950/rounded-swing-jbutton-using-java
    private int radius;

    RoundedBorder(int radius) {
      this.radius = radius;
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(this.radius + 1, this.radius + 1, this.radius + 2, this.radius);
    }

    public boolean isBorderOpaque() {
      return true;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
    }
  }

  class gamePanel extends JPanel {
    static int count = 0;

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g.drawImage(backImg, 0, 0, this); // background image
      t.drawTrack(g2); // Track
      for (Car c : cars) {// Cars
        c.drawCar(g2);
      }
      if (playButtonPressed && !lightsOut) {
        g.drawImage(lightsImg[count], Game.WIDTH / 2, Game.HEIGHT / 2, this);
        count++;
        System.out.println("count: " + count);

        if (count == 5)
          sleep(Math.round(lightsDelay * 3 * Math.random())); // random time
        else if (count == 6)
          lightsOut = true; // end of the lights
        else
          sleep(lightsDelay); // default time
      }
    }
  }

  public void run() {
    update();
    gPanel.repaint();
  }

  static final public void sleep(long time) {
    try {
      Thread.sleep(time); // in milliseconds
    } catch (InterruptedException e) {
      System.out.println(e);
    }
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    if (arg0.getSource() == playButton)
      playButtonPressed = true;
  }
}
