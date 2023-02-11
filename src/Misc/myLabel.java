package Misc;

import java.awt.*;
import javax.swing.*;

import Main.Game;
import Main.Window;

public class myLabel extends JLabel {
  private static int radius = 10;
  public Color colorBackground;
  public Color colorForeground;
  public int x;
  public int y;
  public double xRelative;
  public double yRelative;
  public int width;
  public int height;
  public double widthRelative;
  public double heightRelative;
  public String position;

  public myLabel(String text, int x, int y, int width, int height, Color colorBackground, Color colorForeground) {
    super(text);
    this.colorBackground = colorBackground;
    this.colorForeground = colorForeground;
    this.x = x;
    this.y = y;
    setXRelative(x);
    setYRelative(y);
    this.width = width;
    this.height = height;
    setWidthRelative(width);
    setHeightRelative(height);
    setConfig();
  }

  public myLabel(String text, String position, JFrame f, Color colorBackground, Color colorForeground) {
    super(text);
    this.colorBackground = colorBackground;
    this.colorForeground = colorForeground;
    setConfig();
    this.width = getPreferredSize().width;
    this.height = getPreferredSize().height;
    this.position = position;
    setInitialPosition(position, f);
  }

  public void setInitialPosition(String position, JFrame f) {
    if (position == "topcenter") {
      x = f.getWidth() / 2 - width / 2;
      y = f.getHeight() / 30;
    }
  }

  public void setConfig() {
    setFont(new Font(Game.fontName, Game.fontStyle, Game.fontSize));
  }

  public void setXRelative(int x) {
    xRelative = x * 1. / Window.widthDefaultScreen;
  }

  public void setYRelative(int y) {
    yRelative = y * 1. / Window.heightDefaultScreen;
  }

  public void setWidthRelative(int width) {
    widthRelative = width * 1. / Window.widthDefaultScreen;
  }

  public void setHeightRelative(int height) {
    heightRelative = height * 1. / Window.heightDefaultScreen;
  }

  // public void updateSize(int x, int y, int width, int height) {
  // this.x = x;
  // this.y = y;
  // this.width = width;
  // this.height = height;
  // setBounds(x, y, width, height);
  // // setAlignmentX(CENTER_ALIGNMENT);
  // // setAlignmentY(CENTER_ALIGNMENT);
  // }

  public void updateSize(JFrame f) {
    setXRelative(x);
    setYRelative(y);
    setWidthRelative(width);
    setHeightRelative(height);
    int xNew = (int) Math.round(f.getWidth() * xRelative);
    int yNew = (int) Math.round(f.getHeight() * yRelative);
    int widthNew = (int) Math.round(f.getWidth() * widthRelative);
    int heightNew = (int) Math.round(f.getHeight() * heightRelative);
    setBounds(xNew, yNew, widthNew, heightNew);
    // setAlignmentX(CENTER_ALIGNMENT);
    // setAlignmentY(CENTER_ALIGNMENT);
  }

  @Override
  protected void paintComponent(Graphics g) {
    g.setColor(colorBackground); // Background color
    setForeground(colorForeground); // Foreground color
    g.fillRoundRect(0, 0, getSize().width - 1, getSize().height - 1, 2 * radius, 2 * radius);

    super.paintComponent(g);
  }
}
