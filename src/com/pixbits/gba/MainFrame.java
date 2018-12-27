package com.pixbits.gba;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

public class MainFrame extends JFrame
{
  SourceImage sprite;
  JLabel label;
  
  JLabel size;
  JLabel lColors;
  
  JTextArea output;
  
  JPanel pixelGrid;
      
  
  MainFrame()
  {
    size = new JLabel("Size: ");
    lColors = new JLabel("Colors: "); 
    
    output = new JTextArea(10, 30);
    output.setEditable(false);
    
    
    label = new JLabel();
    label.setHorizontalAlignment(JLabel.CENTER);
    label.setPreferredSize(new Dimension(100,100));
    label.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        )
    );
    
    label.setFocusable(true);
    label.setTransferHandler(new ImageTransferHandler(f -> process(f)));
    
    pixelGrid = new JPanel();
    pixelGrid.setPreferredSize(new Dimension(200, 200));
    pixelGrid.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

    
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(label, BorderLayout.NORTH);
    getContentPane().add(pixelGrid, BorderLayout.CENTER);
    
    JPanel fields = new JPanel();
    fields.setLayout(new BoxLayout(fields, BoxLayout.PAGE_AXIS));
    
    fields.add(size);
    fields.add(lColors);
    
    JScrollPane areaPane = new JScrollPane(output);
    areaPane.setPreferredSize(new Dimension(400,200));
    
    fields.add(areaPane);
    
    fields.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    getContentPane().add(fields, BorderLayout.SOUTH);
    
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    
    setLocationRelativeTo(null);
    setTitle("GBA Sprite Builder");
    pack();
  }
  
  void process(File file)
  {
    try
    {
      sprite = new SourceImage(file.toPath());
    }
    catch (Exception e)
    {
      JOptionPane.showMessageDialog(this, "File doesn't look like a valid image", "Error", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
      return;
    }
    
    try
    {      
      sprite.computeColorIndices();
      
      if (sprite.colorCount() > 15)
      {
        sprite.reduceColors(15);
        sprite.save(file.getParentFile().toPath().resolve("test.png"));
      }
           
      label.setIcon(new ImageIcon(sprite.image()));
      size.setText("Size: "+sprite.width()+"x"+sprite.height());
      lColors.setText("Colors: "+sprite.colorCount());      
      
      output.getDocument().remove(0, output.getDocument().getLength());
      
      output.append("color_t palette[] = { ");
      
      int[] icolors = new int[16];
      icolors[0] = 0x0;
      for (Map.Entry<Integer, Integer> e : sprite.palette().entrySet())
        icolors[e.getValue()] = e.getKey();
      
      SwingUtilities.invokeLater(() -> {
        pixelGrid.removeAll();
        pixelGrid.setLayout(new GridLayout(sprite.width(), sprite.height()));
        
        for (int y = 0; y < sprite.height(); ++y)
          for (int x = 0; x < sprite.width(); ++x)
          {
            int p = sprite.get(x, y);
            int ci = sprite.palette().get(p & 0x00FFFFFF);
            int c = icolors[ci];
            int r = (c >> 16) & 0xFF;
            int g = (c >> 8) & 0xFF;
            int b = c & 0xFF;
            

            JLabel lb = new JLabel(Integer.toHexString(ci));
            lb.setOpaque(true);
            lb.setBackground(new Color(r, g, b));
            lb.setHorizontalAlignment(SwingConstants.CENTER);
            pixelGrid.add(lb);
          }
        
        revalidate();
      });
  
      boolean first = true;
      for (int c : icolors)
      {
        int r = (c >> 16) & 0xFF;
        int g = (c >> 8) & 0xFF;
        int b = c & 0xFF;
        
        r = Math.round(r / 8.0f);
        g = Math.round(g / 8.0f);
        b = Math.round(b / 8.0f);
        
        if (!first)
          output.append(", ");
        
        output.append("color_t("+r+", "+g+", "+b+")");
        first = false;
      }
      
      output.append(" };\n\n");
      
      
      
      output.append("u32 data[] = {\n");
      
      int xx = sprite.width() / 8, yy = sprite.height() / 8;
      
      for (int sy = 0; sy < yy; ++sy)
        for (int sx = 0; sx < xx; ++sx)
        {
          int bx = sx*8, by = sy*8;
          int[] data = new int[8];
          
          for (int y = 0; y < 8; ++y)
          {
            for (int x = 0; x < 8; ++x)
            {
              int p = sprite.get(bx + x, by + y);
              int ci = 0;

              int alpha = (p >> 24) & 0xFF;

              if (alpha == 0xFF)
                ci = sprite.palette().get(p & 0x00FFFFFF);
              
              data[y] |= ci << x*4;
            }
            
            output.append("0x"+String.format("%08X", data[y]));
            
            if (y < 8 - 1)
              output.append(",");
          }
          
          output.append("\n");
        }

      output.append(" };\n");
      
    } 
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
