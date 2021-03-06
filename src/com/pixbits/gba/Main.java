package com.pixbits.gba;

import java.io.File;
import java.nio.file.Paths;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class Main
{
  private static void setLNF()
  {
    try
    {
      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
      {
        if ("Nimbus".equals(info.getName())) {
          UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } 
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  static MainFrame frame;
  
  public static void main(String[] args)
  {
    setLNF();
    
    frame = new MainFrame();
    frame.setVisible(true);
    
    SwingUtilities.invokeLater(() -> frame.process(Paths.get("/Users/jack/Desktop/mirror.png").toFile()));
  }
}
