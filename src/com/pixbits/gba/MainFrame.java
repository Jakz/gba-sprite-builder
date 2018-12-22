package com.pixbits.gba;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.border.BevelBorder;

public class MainFrame extends JFrame
{
  BufferedImage sprite;
  JLabel label;
  
  JLabel size;
  JLabel lColors;
  
  JTextArea output;
  
  Map<Integer, Integer> colors;
  
  
  MainFrame()
  {
    size = new JLabel("Size: ");
    lColors = new JLabel("Colors: "); 
    
    output = new JTextArea(10, 30);
    output.setEditable(false);
    
    
    label = new JLabel();
    label.setHorizontalAlignment(JLabel.CENTER);
    label.setPreferredSize(new Dimension(400,400));
    label.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        )
    );
    
    label.setFocusable(true);
    label.setTransferHandler(new ImageTransferHandler(f -> process(f)));
    
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(label, BorderLayout.CENTER);
    
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
      sprite = ImageIO.read(file);
      if (sprite == null)
        throw new IOException();
    }
    catch (IOException e)
    {
      JOptionPane.showMessageDialog(this, "File doesn't look like a valid image", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    
    try
    {      
      colors = new HashMap<>();
      OctreeQuantizer quantizer = new OctreeQuantizer(8);
      
      for (int x = 0; x < sprite.getWidth(); ++x)
        for (int y = 0; y < sprite.getHeight(); ++y)
        {
          int color = sprite.getRGB(x, y);
          int alpha = (color >> 24) & 0xFF;
          
          if (alpha == 0xFF)
          {
            quantizer.addColor(color & 0x00FFFFFF);
            colors.putIfAbsent(color & 0x00FFFFFF, colors.size() + 1);
          }        
        }
      
      quantizer.buildPalette(16);
      System.out.println(Arrays.toString(quantizer.palette()));
   
      
      label.setIcon(new ImageIcon(sprite));
      size.setText("Size: "+sprite.getWidth()+"x"+sprite.getHeight());
      lColors.setText("Colors: "+colors.size());
      
      output.getDocument().remove(0, output.getDocument().getLength());
      
      output.append("color_t palette[] = { ");
      
      int[] icolors = new int[16];
      icolors[0] = 0x0;
      for (Map.Entry<Integer, Integer> e : colors.entrySet())
        icolors[e.getValue()] = e.getKey();
      
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
        
        output.append("Gfx::rgb15("+r+", "+g+", "+b+")");
        first = false;
      }
      
      output.append(" };\n\n");
      
      
      
      output.append("u32 data[] = {\n");
      
      int xx = sprite.getWidth() / 8, yy = sprite.getHeight() / 8;
      
      for (int sy = 0; sy < yy; ++sy)
        for (int sx = 0; sx < xx; ++sx)
        {
          int bx = sx*8, by = sy*8;
          int[] data = new int[8];
          
          for (int y = 0; y < 8; ++y)
          {
            for (int x = 0; x < 8; ++x)
            {
              int p = sprite.getRGB(bx + x, by + y);
              int ci = 0;

              int alpha = (p >> 24) & 0xFF;

              if (alpha == 0xFF)
                ci = colors.get(p & 0x00FFFFFF);
              
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
