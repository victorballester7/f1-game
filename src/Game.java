import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.BorderFactory.*;
import java.io.*;

public class Game implements KeyListener, ActionListener {
  public static final int widthDefaultScreen = 1920, heightDefaultScreen = 1080;
  // public static final int widthScreen = 1920, heightScreen = 1080;
  public static double screenRatio;
  public static final Color myColor = new Color(255, 74, 74);
  private Image backImg = null;
  private myButton playButton, viewButton;
  private Image playButtonImg = null;
  private Image viewWorldButtonImg = null;
  private Image viewCarButtonImg = null;
  private static int buttonYCoord = 50;
  private static double widthCarView;
  private Image[] lightsImg = new Image[7];
  private static final int lightsImgHeight = 100;
  private static final int lightsImgWidth = lightsImgHeight / 4 * 9;
  public boolean lightsOut = false;
  private static final int lightsDelay = 1000; // in milliseconds
  private JFrame frame;
  private gamePanel mainPanel, smallCircuitPanel;
  private AffineTransform atWorldToScreen;
  private AffineTransform atWorldToSmallScreen;
  private AffineTransform atCarToScreen;
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
    frame.setSize(widthDefaultScreen, heightDefaultScreen);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // use x button to close the window
    frame.setLocationRelativeTo(null); // centers the window on the screen
    // frame.setResizable(false); // prevent the window to be resizable
    frame.addKeyListener(this);

    screenRatio = frame.getWidth() * 1. / frame.getHeight();

    t = new OvalTrack(17, 300, 300, new Vector2D(1, 0));
    widthCarView = Track.widthTrack * 2;

    cars[0] = new McLaren(1, t, true);
    cars[1] = new Ferrari(2, t, false);
    cars[2] = new RedBull(3, t, false);
    cars[3] = new McLaren(4, t, false);
    cars[4] = new Ferrari(5, t, false);
    cars[5] = new RedBull(6, t, false);

    myCar = cars[0];

    try {
      // background
      backImg = ImageIO.read(new File("images/misc/grass.jpg"));
      backImg = backImg.getScaledInstance(frame.getWidth(), frame.getHeight(), Image.SCALE_DEFAULT);

      // lights
      lightsImg[0] = ImageIO.read(new File("images/lights/off.png"));
      for (int i = 1; i < 6; i++) {
        lightsImg[i] = ImageIO.read(new File("images/lights/red" + i + ".png"));
      }
      lightsImg[6] = ImageIO.read(new File("images/lights/green.png"));
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
    mainPanel = new gamePanel(true);
    smallCircuitPanel = new gamePanel(false);
    frame.setContentPane(mainPanel);
    frame.add(smallCircuitPanel);
    // Play button
    frame.setLayout(null); // necessary to display the button in a correct position
    // configButton(playButton, playButtonImg, (WIDTH - playButtonImgWidth) / 2, 50);
    // configButton(viewButton, vie);
    // configPlayButton();
    playButton = new myButton(frame, "Start", playButtonImg, "center");
    playButton.addActionListener(this);
    viewButton = new myButton(frame, "Track view", viewWorldButtonImg, "left");
    viewButton.addSecondTextImage("Car view", viewCarButtonImg);
    viewButton.addActionListener(this);
    frame.add(playButton);
    frame.add(viewButton);
  }

  public void show() {
    frame.setVisible(true); // activates the visibility
  }

  public void update() {
    // System.out.println("position " + myCar.position.print());
    // System.out.println("position finish line " + Track.finishLinePoint.print());
    // System.out.println("height frame: " + frame.getHeight());
    screenRatio = frame.getWidth() * 1. / frame.getHeight();
    // if (viewButton.isPressed)
    worldToScreenTransform();
    worldToSmallScreenTransform();
    // else
    carToScreenTransform();
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
    mainPanel.repaint();
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

    atWorldToScreen.translate(frame.getWidth() / 2, frame.getHeight() / 2);
    atWorldToScreen.scale(frame.getWidth() / t.bounds.getWidth(), frame.getHeight() / t.bounds.getHeight());
    atWorldToScreen.translate(-t.bounds.getCenterX(), -t.bounds.getCenterY());
  }

  public void worldToSmallScreenTransform() {
    atWorldToSmallScreen = new AffineTransform();

    atWorldToSmallScreen.translate(smallCircuitPanel.getWidth() / 2, smallCircuitPanel.getHeight() / 2);
    atWorldToSmallScreen.scale(smallCircuitPanel.scale * frame.getWidth() / t.bounds.getWidth(), smallCircuitPanel.scale * frame.getHeight() / t.bounds.getHeight());
    atWorldToSmallScreen.translate(-t.bounds.getCenterX(), -t.bounds.getCenterY());
  }

  public void carToScreenTransform() {
    // double angle = Vector2D.signedAngle(myCar.direction, t.startDirection);
    double ratio = frame.getWidth() / widthCarView;
    atCarToScreen = new AffineTransform();

    atCarToScreen.translate(frame.getWidth() / 2, frame.getHeight() / 2);
    atCarToScreen.scale(ratio, ratio);
    atCarToScreen.rotate(-(myCar.rotationAngle + Car.initialSteeringAngle));
    atCarToScreen.translate(-myCar.position.x, -myCar.position.y);
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
      if (viewButton.isPressed)
        smallCircuitPanel.setVisible(false);
      else
        smallCircuitPanel.setVisible(true);
    }
  }

  class gamePanel extends JPanel {
    private int count = 0;
    private boolean isPrincipal;
    private double scale = 0.3;
    public int x;
    public int y;
    private static final int borderThickness = 10;
    private static final int sizeCarPoints = 10;

    public gamePanel(boolean isPrincipal) {
      this.isPrincipal = isPrincipal;
      if (!isPrincipal) {
        updateSize();
        setBorder(BorderFactory.createLineBorder(myColor, borderThickness, true));
      }
    }

    public void updateSize() {
      x = frame.getWidth() / 50;
      y = frame.getHeight() / 10;
      setBounds(x, y, (int) Math.round(scale * frame.getWidth()), (int) Math.round(scale * frame.getHeight()));

    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // better resolution
      g.drawImage(backImg, 0, 0, this); // background image
      if (isPrincipal) { // main panel
        if (viewButton.isPressed) {
          t.drawTrack(g2, atWorldToScreen); // Track
          for (Car c : cars) {// Cars
            c.drawCar(g2, atWorldToScreen);
          }
        } else {
          t.drawTrack(g2, atCarToScreen); // Track
          for (Car c : cars) {// Cars
            c.drawCar(g2, atCarToScreen);
          }
        }
      } else { // small panel
        t.drawTrack(g2, atWorldToSmallScreen); // Track
        for (Car c : cars) {// Cars
          Point2D transPoint = atWorldToSmallScreen.transform(c.position.toPoint(), null);
          g2.setColor(c.color);
          g2.fill(new Ellipse2D.Double(transPoint.getX(), transPoint.getY(), sizeCarPoints, sizeCarPoints));
        }
      }
      smallCircuitPanel.updateSize();
      playButton.updateSize(frame);
      viewButton.updateSize(frame);
      // playButton.repaint((frame.getWidth() - playButton.getWidth()) / 2, (frame.getHeight() - playButton.getHeight()) / 10, playButton.getWidth(), playButton.getHeight());

      // lights
      if (isPrincipal) {
        if (count > 0 && count < 6)
          sleep(lightsDelay); // default time
        else if (count == 6)
          sleep(Math.round(lightsDelay * 3 * Math.random())); // random time
        else if (count == 7)
          lightsOut = true;

        if (playButton.isPressed && !lightsOut) {
          g.drawImage(lightsImg[count], (frame.getWidth() - lightsImgWidth) / 2, buttonYCoord, this);
          count++;
        } else if (count < 200 && count > 0) { // extra time for the green lights to disappear
          g.drawImage(lightsImg[6], (frame.getWidth() - lightsImgWidth) / 2, buttonYCoord, this);
          count++;
        }
      }
    }
  }
}
