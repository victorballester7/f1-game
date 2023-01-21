import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

public class RoundButton1 extends javax.swing.JFrame {

  // Creates new form RoundButton
  public RoundButton1() {
    initComponents();
  }

  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">
  private void initComponents() {
  }

  public static void main(String args[]) throws UnsupportedLookAndFeelException, ClassNotFoundException {
    // try-catch block to handle InstantiationException
    try {
      // Here you can select the selected theme class name in JTatt
      UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
    } catch (InstantiationException ex) {
      Logger.getLogger(RoundButton1.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(RoundButton1.class.getName()).log(Level.SEVERE, null, ex);
    }

    java.awt.EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        new RoundButton1().setVisible(true);
      }

    });
    // </editor-fold>

  }

  // Variables declaration - do not modify
  private javax.swing.JButton jButton1;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JButton testbtn2;
  private javax.swing.JButton testbtn3;
  // End of variables declaration
}
