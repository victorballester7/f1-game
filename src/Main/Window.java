package Main;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import Cars.Car;
import Misc.ClassificationBoard;
import Misc.LapCounter;
import Misc.myButton;
import Misc.myLabel;
import Misc.myOptionPane;

import java.io.*;

public class Window implements KeyListener, ActionListener {
  public static final int FPS = 60;
  public static final int widthDefaultScreen = 1920, heightDefaultScreen = 1080;
  // public static final int widthScreen = 1920, heightScreen = 1080;
  public static double screenRatio;
  public static myButton playButton, viewButton;
  private Image playButtonImg = null;
  private Image viewWorldButtonImg = null;
  private Image viewCarButtonImg = null;
  private Image changeViewCarImg = null;
  public static JFrame frame;
  public static GamePanel mainPanel, smallCircuitPanel;
  public static ClassificationBoard classification;
  public static LapCounter lapLabel;
  public static Color lapLabelBackground = new Color(50, 50, 50);
  public static Color lapLabelForeground = new Color(220, 220, 220);
  public static myOptionPane carViewMenu;
  private static Game game;

  public Window() {
    frame = new JFrame();
    frame.setTitle("F1 game"); // Name of the window
    frame.setSize(widthDefaultScreen, heightDefaultScreen);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // use x button to close the window
    frame.setLocationRelativeTo(null); // centers the window on the screen
    // frame.setResizable(false); // prevent the window to be resizable
    frame.addKeyListener(this);

    screenRatio = frame.getWidth() * 1. / frame.getHeight();
    try {
      // Play button
      playButtonImg = ImageIO.read(new File("images/misc/flagPlayButton.png"));
      viewWorldButtonImg = ImageIO.read(new File("images/misc/worldViewButton.png"));
      viewCarButtonImg = ImageIO.read(new File("images/misc/carViewButton.png"));
      changeViewCarImg = ImageIO.read(new File("images/misc/carViewButton2.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    game = new Game();

    mainPanel = new GamePanel(true);
    smallCircuitPanel = new GamePanel(false);
    frame.setContentPane(mainPanel);
    frame.add(smallCircuitPanel);
    // Play button
    // frame.setLayout(null); // necessary to display the button in a correct position
    // configButton(playButton, playButtonImg, (WIDTH - playButtonImgWidth) / 2,
    // 50);
    // configButton(viewButton, vie);
    // configPlayButton();

    playButton = new myButton("Start", playButtonImg, "center");
    playButton.addActionListener(this);
    viewButton = new myButton("Track view", viewWorldButtonImg, "left");
    viewButton.addSecondTextImage("Car view", viewCarButtonImg);
    viewButton.addActionListener(this);
    frame.add(playButton);
    frame.add(viewButton);

    // JLabel label = new JLabel("Sample text");
    // label.setFont(label.getFont().deriveFont(20.0F));
    // label.setForeground(Color.white);
    // frame.setLayout(null); // necessary to display the button in a correct position

    classification = new ClassificationBoard(game.cars, 200, 500);
    for (JLabel label : classification.labels)
      frame.add(label);
    classification.hide();

    lapLabel = new LapCounter(Game.totalLaps, "topcenter", frame, lapLabelBackground, lapLabelForeground);
    // lapLabel = new myLabel(" LAP 0 / " + Game.totalLaps + " ", 500, 500, 200, 20, lapLabelBackground, lapLabelForeground);
    frame.add(lapLabel);
    lapLabel.setVisible(false);
    // carViewMenu = new myOptionPane(frame, "Change car view", changeViewCarImg);

    // for (Car c : game.cars) {
    // carViewMenu.add(c.getClass().toString());
    // }
    // frame.add(carViewMenu);

    // carViewMenu = new JOptionPane("Prova");
    // frame.getContentPane().add(carViewMenu);

  }

  public void show() {
    frame.setVisible(true); // activates the visibility
  }

  public class GamePanel extends JPanel {
    private int count = 0;
    private boolean isPrincipal;
    public double scale = 0.3;
    public int x;
    public int y;
    private static final int borderThickness = 10;
    private static final int sizeCarPoints = 30;

    public GamePanel(boolean isPrincipal) {
      this.isPrincipal = isPrincipal;
      if (!isPrincipal) {
        updateSize();
        setBorder(BorderFactory.createLineBorder(Game.myColor, borderThickness, true));
      }
    }

    public void updateSize() {
      x = frame.getWidth() / 50;
      y = frame.getHeight() / 10;
      setBounds(x, y, (int) Math.round(scale * frame.getWidth()), (int) Math.round(scale * frame.getHeight()));
    }

    public void update(Graphics g) {
      paint(g);
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // better resolution
      g.drawImage(Game.backImg, 0, 0, this); // background image
      if (isPrincipal) { // main panel
        if (viewButton.isPressed) { // Whole-circuit view
          game.t.drawTrack(g2, game.atWorldToScreen); // Track
          // for (Car c : game.cars) {// Cars
          // c.drawCar(g2, game.atWorldToScreen);
          // }
          for (Car c : game.cars) {// Cars
            Point2D transPoint = game.atWorldToScreen.transform(c.position.toPoint(), null);
            g2.setColor(c.color);
            g2.fill(new Ellipse2D.Double(transPoint.getX() - sizeCarPoints / 2, transPoint.getY() - sizeCarPoints / 2, sizeCarPoints, sizeCarPoints));
          }
        } else { // Car view
          game.t.drawTrack(g2, game.atCarToScreen); // Track
          for (Car c : game.cars) {// Cars
            c.drawCar(g2, game.atCarToScreen);
          }
        }
      } else { // small panel
        game.t.drawTrack(g2, game.atWorldToSmallScreen); // Track
        for (Car c : game.cars) {// Cars
          Point2D transPoint = game.atWorldToSmallScreen.transform(c.position.toPoint(), null);
          g2.setColor(c.color);
          g2.fill(new Ellipse2D.Double(transPoint.getX() - sizeCarPoints * scale / 2, transPoint.getY() - sizeCarPoints * scale / 2, sizeCarPoints * scale, sizeCarPoints * scale));
        }
      }
      smallCircuitPanel.updateSize();
      playButton.updateSize(frame);
      viewButton.updateSize(frame);
      classification.updateSize(frame, game.cars);
      lapLabel.updateSize(frame);

      // lights
      if (isPrincipal) {
        if (count > 0 && count < 6)
          Game.sleep(Game.lightsDelay); // default time
        else if (count == 6)
          Game.sleep(Math.round(Game.lightsDelay * 3 * Math.random())); // random time
        else if (count == 7)
          game.lightsOut = true;

        if (playButton.isPressed && !game.lightsOut) {
          g.drawImage(game.lightsImg[count], (frame.getWidth() - Game.lightsImgWidth) / 2, frame.getHeight() / 15, this);
          count++;
        } else if (count < 200 && count > 0) { // extra time for the green lights to disappear
          g.drawImage(game.lightsImg[6], (frame.getWidth() - Game.lightsImgWidth) / 2, frame.getHeight() / 15, this);
          count++;
        }
      }
    }
  }

  public static void main(String[] args) {
    Window win = new Window();
    win.show();
    game.run();
  }

  @Override
  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
    case 37: // left arrow
      game.keyLeft = true;
      // System.out.println("left");
      // cars[0].rotationAngle = -Car.rotationRate;
      break;
    case 38: // up arrow
      game.keyUp = true;
      // System.out.println("up");
      break;
    case 39: // right arrow
      game.keyRight = true;
      // System.out.println("right");
      // cars[0].rotationAngle = Car.rotationRate;
      break;
    case 40: // down arrow
      game.keyDown = true;
      // System.out.println("down");
      break;
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
    switch (e.getKeyCode()) {
    case 37: // left arrow
      game.keyLeft = false;
      break;
    case 38: // up arrow
      game.keyUp = false;
      break;
    case 39: // right arrow
      game.keyRight = false;
      break;
    case 40: // down arrow
      game.keyDown = false;
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
    if (button == Window.playButton) {
      Window.playButton.isPressed = true;
      Window.frame.remove(Window.playButton);
      classification.show();
      lapLabel.setVisible(true);
    } else if (button == Window.viewButton) {
      Window.viewButton.isPressed = !Window.viewButton.isPressed;
      Window.viewButton.modify();
      if (Window.viewButton.isPressed)
        Window.smallCircuitPanel.setVisible(false);
      else
        Window.smallCircuitPanel.setVisible(true);
    }
  }
}
