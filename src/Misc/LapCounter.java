package Misc;

import java.awt.Color;

import javax.swing.JFrame;

public class LapCounter extends myLabel {
  public int totalLaps;
  public int currentLap = 0;

  public LapCounter(int totalLaps, String position, JFrame f, Color colorBackground, Color colorForeground) {
    super(" LAP " + 0 + " / " + totalLaps + " ", position, f, colorBackground, colorForeground);
    this.totalLaps = totalLaps;
  }

  public void increment() {
    currentLap++;
    if (currentLap == totalLaps) {
      setText(" LAST LAP ");
    } else if (currentLap > totalLaps) {
      setText(" END OF THE RACE ");
    } else {
      setText(" LAP " + currentLap + " / " + totalLaps + " ");
    }
    setSize(getPreferredSize());
    width = getPreferredSize().width;
    height = getPreferredSize().height;
  }

}
