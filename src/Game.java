import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.*;

public class Game implements KeyListener, ActionListener {
  public static final int WIDTH = 1920, HEIGHT = 1080;
  public static final int widthScreen = 1920, heightScreen = 1080;
  public static final double screenRatio = widthScreen * 1. / heightScreen;
  private Image backImg = null;
  private myButton playButton, viewButton;
  private Image playButtonImg = null;
  private Image viewWorldButtonImg = null;
  private Image viewCarButtonImg = null;
  private static final double widthCarView = 200;
  private Image[] lightsImg = new Image[7];
  private static final int lightsImgHeight = 100;
  private static final int lightsImgWidth = lightsImgHeight / 4 * 9;
  public boolean lightsOut = false;
  private static final int lightsDelay = 1000; // in milliseconds
  private JFrame frame;
  private gamePanel gPanel;
  private AffineTransform atWorldToScreen;
  private AffineTransform atCarToWorld;
  public static final int NumCars = 6;
  private Track t;
  public static Car[] cars = new Car[NumCars];
  public Car myCar;
  public boolean keyLeft = false;
  public boolean keyRight = false;
  public boolean keyUp = false;
  public boolean keyDown = false;

  public Game() {
    initialize();
  }

  private void initialize() {
    frame = new JFrame();
    frame.setTitle("F1 game"); // Name of the window
    frame.setSize(WIDTH, HEIGHT);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // use x button to close the window
    frame.setLocationRelativeTo(null); // centers the window on the screen
    // frame.setResizable(false); // prevent the window to be resizable
    frame.addKeyListener(this);

    t = new OvalTrack(100, 300, 300, new Vector2D(1, 0));

    cars[0] = new Mercedes(1, t, true);
    cars[1] = new Ferrari(2, t, false);
    cars[2] = new RedBull(3, t, false);
    cars[3] = new Mercedes(4, t, false);
    cars[4] = new Ferrari(5, t, false);
    cars[5] = new RedBull(6, t, false);

    myCar = cars[0];

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
        lightsImg[i] = lightsImg[i].getScaledInstance(lightsImgWidth, lightsImgHeight, Image.SCALE_DEFAULT);
      }

      // Play button
      // playButtonImg = ImageIO.read(new File("images/misc/playButton.png"));
      // scale = playButtonImgWidth * 1. / playButtonImg.getWidth(frame);
      // playButtonImg = playButtonImg.getScaledInstance(playButtonImgWidth, (int) Math.round(playButtonImg.getHeight(frame) * scale), Image.SCALE_DEFAULT);
      playButtonImg = ImageIO.read(new File("images/misc/flagPlayButton.png"));
      viewWorldButtonImg = ImageIO.read(new File("images/misc/worldViewButton.png"));
      viewCarButtonImg = ImageIO.read(new File("images/misc/carViewButton.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    gPanel = new gamePanel();
    frame.setContentPane(gPanel);

    // Play button
    frame.setLayout(null); // necessary to display the button in a correct position
    // configButton(playButton, playButtonImg, (WIDTH - playButtonImgWidth) / 2, 50);
    // configButton(viewButton, vie);
    // configPlayButton();
    playButton = new myButton("Start", playButtonImg, WIDTH / 2, 50);
    playButton.addActionListener(this);
    viewButton = new myButton("World view", viewWorldButtonImg, 100, 50);
    viewButton.addSecondTextImage("Car view", viewCarButtonImg);
    viewButton.addActionListener(this);
    frame.add(playButton);
    frame.add(viewButton);
  }

  public void show() {
    frame.setVisible(true); // activates the visibility
  }

  public void update() {
    System.out.println("width frame: " + frame.getWidth());
    System.out.println("height frame: " + frame.getHeight());
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

  public boolean isWorldView() {
    return viewButton.isPressed;
  }

  public void worldToScreenTransform() {
    atWorldToScreen = new AffineTransform();

    atWorldToScreen.translate(widthScreen / 2, heightScreen / 2);
    atWorldToScreen.scale(widthScreen / t.bounds.getWidth(), heightScreen / t.bounds.getHeight());
    atWorldToScreen.translate(-t.bounds.getCenterX(), -t.bounds.getCenterX());
  }

  public void transform() {
    atCarToWorld = new AffineTransform();
    atCarToWorld.translate(-myCar.position.x, -myCar.position.y);
    atCarToWorld.translate(-myCar.position.x, -myCar.position.y);
  }

  @Override
  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
    case 37: // left arrow
      keyLeft = true;
      // System.out.println("left");
      // cars[0].rotationAngle = -Car.rotationRate;
      break;
    case 38: // up arrow
      keyUp = true;
      // System.out.println("up");
      break;
    case 39: // right arrow
      keyRight = true;
      // System.out.println("right");
      // cars[0].rotationAngle = Car.rotationRate;
      break;
    case 40: // down arrow
      keyDown = true;
      // System.out.println("down");
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

  @Override
  public void actionPerformed(ActionEvent arg0) {
    Object button = arg0.getSource();
    if (button == playButton) {
      playButton.isPressed = true;
      frame.remove(playButton);
    } else if (button == viewButton) {
      viewButton.isPressed = !viewButton.isPressed;
      viewButton.modify();
    }
  }

  class gamePanel extends JPanel {
    private int count = 0;

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g.drawImage(backImg, 0, 0, this); // background image
      t.drawTrack(g2); // Track
      for (Car c : cars) {// Cars
        c.drawCar(g2);
      }
      if (count > 0 && count < 6)
        sleep(lightsDelay); // default time
      else if (count == 6)
        sleep(Math.round(lightsDelay * 3 * Math.random())); // random time
      else if (count == 7)
        lightsOut = true;

      if (playButton.isPressed && !lightsOut) {
        g.drawImage(lightsImg[count], (Game.WIDTH - lightsImgWidth) / 2, (Game.HEIGHT - lightsImgHeight) / 2, this);
        count++;
      } else if (count < 30 && count > 0) {
        g.drawImage(lightsImg[6], (Game.WIDTH - lightsImgWidth) / 2, (Game.HEIGHT - lightsImgHeight) / 2, this);
        count++;
      }
    }
  }
}
