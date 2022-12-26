import javax.swing.SwingUtilities;

public class Window {
  Game win;

  public Window() {
    win = new Game();
    win.show();
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        win.update();
      }
    });
  }

  public static void main(String[] args) {
    new Window();
  }

}
