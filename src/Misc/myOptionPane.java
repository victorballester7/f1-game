package Misc;

import java.awt.*;

import javax.swing.*;

import Main.Game;

public class myOptionPane extends JOptionPane {
  private Color b = Game.myColor;
  // private Color bPressed = new Color(161, 39, 39);
  // private Color f = new Color(240, 240, 240);
  private ImageIcon img;
  // private String text, text2;
  // private static final int fontSize = 20;
  // private String position;
  // private int x; // x-coordinate of the center of the button
  // private int y; // y-coordinate of the center of the button
  // public boolean isPressed = false;

  public myOptionPane(JFrame f, String text, Image img) {
    super(text);
    setMessage("Change car view");
    this.img = new ImageIcon(img.getScaledInstance(Game.iconSize, Game.iconSize, Image.SCALE_DEFAULT));
    setConfig(null);
  }

  public void setConfig(JFrame f) {
    setIcon(img);
    setBackground(b);
    setFont(new Font(Game.fontName, Game.fontStyle, Game.fontSize));
    // setSize(getPreferredSize().width, getPreferredSize().height);
    updateSize(f);
  }

  public void updateSize(JFrame f) {
    // if (position == "center") {
    // x = f.getWidth() / 2 - getWidth() / 2;
    // y = f.getHeight() / 30;
    // } else if (position == "left") {
    // x = f.getWidth() / 50;
    // y = f.getHeight() / 30;
    // }
    // setBounds(x, y, getWidth(), getHeight());
    setBounds(500, 500, getWidth(), getHeight());
  }

}
