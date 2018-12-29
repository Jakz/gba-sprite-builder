package com.pixbits.gba;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
  JLabel dropLabel;
  
  JLabel infoLabel;
  
  JTextArea output;
  
  JPanel pixelGrid;
  JPanel pixelPalette;
  
  JPanel exportCheckboxes;
  JPanel exportButtons;
  
  private SplitMode splitMode;
  private JComboBox<SplitMode> splitModeComboBox;
  private JCheckBox showIndexInPixelGrid;
  
  JPanel buildPixelGridPanel()
  { 
    //pixelGrid.setPreferredSize(new Dimension(200, 200));
    
    GridBagHolder h = new GridBagHolder();

    JPanel container = new JPanel(new GridBagLayout());
    container.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    
    h.w(1.0f, 1.0f);
    

    pixelGrid = new JPanel();
    h.g(0,0).wh(10,9).p(100).fill();
    container.add(pixelGrid, h.c());
    
    pixelPalette = new JPanel();
    h.g(0,10).wh(10,1).py(16).hfill();
    container.add(pixelPalette, h.c());
   
    showIndexInPixelGrid = new JCheckBox("Show color indices");
    showIndexInPixelGrid.addActionListener(e -> refreshPixelGrid());
    container.add(showIndexInPixelGrid, h.p(0).noFill().g(9,11).a("tr").wh(1,1).c());
    
    infoLabel = new JLabel("Info: N/A");
    container.add(infoLabel, h.xy(0,12).w(6).a("tl").c());
       
    return container;
  }
  
  void splitModeChanged()
  {
    splitMode = splitModeComboBox.getItemAt(splitModeComboBox.getSelectedIndex());
    refreshPixelGrid();
    
    SplitMode.SpriteCoord sc = splitMode.iterator();
    
    while (sc != null)
    {
      System.out.println(sc.x +", "+sc.y);
      sc = sc.next();
    }
  }
  
  void buildSplitModes()
  {
    int tw = sprite.width(), th = sprite.height();
    int sw = tw / 8, sh = th / 8;
    
    splitModeComboBox.removeAllItems();
    
    int[] steps = { 1, 2, 4, 8 };
    
    for (int sx : steps)
      for (int sy : steps)
      {
        if (sw % sx == 0 && sh % sy == 0)
        {
          final int mw = sw / sx, mh = sh / sy;
          
          if (mw > 1)
            splitModeComboBox.addItem(new SplitMode(sx, sy, true, mw, mh));
          
          if (mh > 1)
            splitModeComboBox.addItem(new SplitMode(sx, sy, false, mw, mh));
          
          if (mw == 1 && mh == 1)
            splitModeComboBox.addItem(new SplitMode(sx, sy, false, mw, mh));

        }
        

      }
   
    splitMode = splitModeComboBox.getItemAt(0);
  }
      
  
  MainFrame()
  {

    output = new JTextArea(10, 30);
    output.setEditable(false);
    
    
    dropLabel = new JLabel();
    dropLabel.setHorizontalAlignment(JLabel.CENTER);
    dropLabel.setPreferredSize(new Dimension(100,100));
    dropLabel.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        )
    );
    
    dropLabel.setText("Drop a PNG here");
    dropLabel.setFocusable(true);
    dropLabel.setTransferHandler(new ImageTransferHandler(f -> process(f)));
    
    
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(dropLabel, BorderLayout.NORTH);

    getContentPane().add(buildPixelGridPanel(), BorderLayout.CENTER);
    
    JPanel fields = new JPanel();
    fields.setLayout(new BoxLayout(fields, BoxLayout.PAGE_AXIS));
            
    JScrollPane areaPane = new JScrollPane(output);
    areaPane.setPreferredSize(new Dimension(400,200));
    
    fields.add(areaPane);
    
    fields.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    exportCheckboxes = new JPanel();
    
    splitModeComboBox = new JComboBox<>();
    splitModeComboBox.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) splitModeChanged(); });
    exportCheckboxes.add(splitModeComboBox);
    
    JCheckBox hex = new JCheckBox("hex");
    exportCheckboxes.add(hex);
    JCheckBox pad = new JCheckBox("pad");
    exportCheckboxes.add(pad);
    JCheckBox space = new JCheckBox("space");
    exportCheckboxes.add(space);
    
    hex.setSelected(true);
    pad.setSelected(true);
    space.setSelected(false);
    
    fields.add(exportCheckboxes);
    
    BiFunction<String, Integer, JButton> generateExportButton = (label, i) -> {
      JButton export = new JButton(label);
      export.addActionListener(e -> {
        String value = exportAsByteArray(i, 80, hex.isSelected(), pad.isSelected(), space.isSelected());
        StringSelection sel = new StringSelection(value);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
      });
      export.setEnabled(false);
      return export;
    };
    
    exportButtons = new JPanel();
    exportButtons.add(generateExportButton.apply("uint8_t", 1));
    exportButtons.add(generateExportButton.apply("uint16_t", 2));
    exportButtons.add(generateExportButton.apply("uint32_t", 4));
    fields.add(exportButtons);
    
    getContentPane().add(fields, BorderLayout.SOUTH);
    
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    
    setLocationRelativeTo(null);
    setTitle("GBA Sprite Builder");
    pack();
  }
  
  int[] icolors;
  Color[] colors;
  
  private Color contrastColor(Color color)
  {
    int d = 0;

    double luminance = ( 0.299f * color.getRed() + 0.587f * color.getGreen() + 0.114f * color.getBlue())/255;
    
    if (luminance > 0.5f)
       d = 0; 
    else
       d = 255;

    return new Color(d, d, d);
  }

  void refreshPixelGrid()
  {
    boolean showColorIndices = showIndexInPixelGrid.isSelected();
    
    pixelGrid.removeAll();
    
    if (sprite != null)
    {
      pixelGrid.setLayout(new GridLayout(sprite.height(), sprite.width())); 
      
      SplitMode mode = (SplitMode)splitModeComboBox.getSelectedItem();
      int tw = 8, th = 8;
      
      if (mode != null)
      {
        tw = mode.tilesWide()*8;
        th = mode.tilesHigh()*8;
      }

      
      for (int y = 0; y < sprite.height(); ++y)
        for (int x = 0; x < sprite.width(); ++x)
        {
          JLabel lb = new JLabel();
          lb.setFont(new Font("monospaced", Font.PLAIN, 8));
          lb.setOpaque(true);
          
          int p = sprite.get(x, y);
          
          if ((p & 0xFF000000) == 0xFF000000)
          {
            int ci = sprite.palette().get(p & 0x00FFFFFF);
            Color c = colors[ci];
            lb.setBackground(c);
            
            if (showColorIndices)
            {
              lb.setForeground(contrastColor(c));
              lb.setText(Integer.toHexString(ci));
            }
          }
          else
            if (showColorIndices)
              lb.setText("0");
          
          int topBorder = y % 8 == 0 ? 1 : 0;
          int bottomBorder = y == sprite.height() - 1 ? 4 : 0;
          int leftBorder = x % 8 == 0 ? 1 : 0;
          int rightBorder = x == sprite.width() - 1 ? 4 : 0;
          
          if (y % th == 0) topBorder *= 4;
          if (x % tw == 0) leftBorder *= 4;
     
          lb.setBorder(BorderFactory.createMatteBorder(topBorder, leftBorder, bottomBorder, rightBorder, Color.RED));
               
          lb.setHorizontalAlignment(SwingConstants.CENTER);
          
          int s = Math.max(lb.getSize().width, lb.getSize().height);
          lb.setPreferredSize(new Dimension(10,10));
          
          
          pixelGrid.add(lb);
        }
    }
    
    pack();
    revalidate();
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
      final int COLOR_COUNT = 16;
      
      
      sprite.computeColorIndices();
      int originalColorCount = sprite.colorCount();
      
      /* if image has more colors than allowed we need to reduce them */
      if (originalColorCount > COLOR_COUNT-1)
      {
        sprite.reduceColors(COLOR_COUNT-1);
        //sprite.save(file.getParentFile().toPath().resolve("test.png"));
      }
      
      sprite.sortColorIndices();
           
      dropLabel.setText(null);
      dropLabel.setIcon(new ImageIcon(sprite.image()));
      
      infoLabel.setText(String.format("size: %dx%d, tiles: %d, colors: %d (%d)", sprite.width(), sprite.height(), sprite.width()*sprite.height()/64, sprite.colorCount(), originalColorCount));
   
      icolors = new int[COLOR_COUNT];
      colors = new Color[icolors.length];
      icolors[0] = 0x0;
            /* sort by hue/brightness/luminance */
     
      for (Map.Entry<Integer, Integer> e : sprite.palette().entrySet())
      {
        int c = e.getKey();

        icolors[e.getValue()] = c;
       
        int r = (c >> 16) & 0xFF;
        int g = (c >> 8) & 0xFF;
        int b = c & 0xFF;
        
        colors[e.getValue()] = new Color(r, g, b);
      }
      
      
      for (Component c : exportButtons.getComponents())
        c.setEnabled(true);
      
      
      pixelPalette.removeAll();
      pixelPalette.setLayout(new GridLayout(1, icolors.length));
      
      for (int i = 0; i < 16; ++i)
      {
        JLabel lb = new JLabel();
        lb.setOpaque(true);
        lb.setBackground(colors[i]);
        
        if (colors[i] != null)
          lb.setForeground(contrastColor(colors[i]));
        lb.setText(Integer.toHexString(i));
        lb.setHorizontalAlignment(SwingConstants.CENTER);
        pixelPalette.add(lb);
      }
      
      pixelPalette.revalidate();
      
      buildSplitModes();
      refreshPixelGrid();
       
      output.getDocument().remove(0, output.getDocument().getLength());
      
      output.append("color_t palette[] = { ");
  
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
    } 
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  
  String exportAsByteArray(int dataSize, int wrap, boolean hex, boolean pad, boolean space)
  {
    StringBuilder str = new StringBuilder();
    
    if (dataSize == 1)
      str.append("uint8_t data[] = {\n  ");
    else if (dataSize == 2)
      str.append("uint16_t data[] = {\n  ");
    else if (dataSize == 4)
      str.append("uint32_t data[] = {\n  ");

    String formatString = "";
    
    if (hex && pad)
      formatString = "0x%0" + dataSize*2 + "X";
    else if (hex)
      formatString = "0x%X";
    else
      formatString = "%d";

    int xx = sprite.width() / 8, yy = sprite.height() / 8;

    /* for each 8x8 subtile */
    for (int sy = 0; sy < yy; ++sy)
      for (int sx = 0; sx < xx; ++sx)
      {
        int bx = sx*8, by = sy*8;
        int data = 0;
        int s = 0;
        
        
        for (int y = 0; y < 8; ++y)
        {
          for (int x = 0; x < 8; ++x)
          {
            int p = sprite.get(bx + x, by + y);
            int ci = 0;

            int alpha = (p >> 24) & 0xFF;

            if (alpha == 0xFF)
              ci = sprite.palette().get(p & 0x00FFFFFF);
            
            data |= ci << s;
            
            if ((x+1) % (dataSize*2) == 0)
            {
              if (data == 0 && !pad)
                str.append("0");
              else
                str.append(String.format(formatString, data));
              
              str.append(",");
              
              if (space)
                str.append(" ");
              
              data = 0;
              s = 0;
            }
            else
              s += 4;
          }
          
        }
        
        if (space)
          str.delete(str.length()-1, str.length());
        str.append("\n  ");
      }
    
    str.delete(str.length()-2, str.length());

    str.append(" };\n");
    
    return str.toString();
  }
}
