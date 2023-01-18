import javax.swing.*;
import java.awt.*;

public class Main {
  public static void main(String arg[]) {
    JFrame f = new JFrame("SetBounds Example");
    f.setSize(300, 300);
    // Set the layout to null
    f.setLayout(null);
    // Create button
    JButton btn = new JButton("Welcome To StackHowTo!");
    // Define the position and size of the button
    btn.setBounds(80, 80, 200, 40);
    f.add(btn);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setLocationRelativeTo(null);
    f.setVisible(true);
  }
}