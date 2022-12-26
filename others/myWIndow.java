import javax.swing.*;

class myWindow {
  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setTitle("myWindow");
    f.setSize(640, 480);
    // Container c = f.getContentPane();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    ImageIcon icon = new ImageIcon("images/car_ferrari.jpg");
    JLabel label = new JLabel(icon);
    label.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());
    f.add(label);
    f.setVisible(true);
  }
}