public class Window {
  public static final int FRAMES = 60;
  Game game;

  public Window() {
    game = new Game();
    game.show();
    while (true) {
      game.run();
      Game.sleep(1000 / FRAMES);
    }
    // SwingUtilities.invokeLater(new Runnable() {
    // @Override
    // public void run() {
    // game.update();
    // }
    // });
  }

  public static void main(String[] args) {
    new Window();
  }
}
