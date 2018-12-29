package com.pixbits.gba;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

public class SourceImage
{
  private final BufferedImage image;
  private final Map<Integer, Integer> colorIndices;
  
  private void log(String format, Object... args)
  {
    System.out.println(String.format(format, args));
  }
  
  public SourceImage(Path path) throws IOException
  {
    image = ImageIO.read(path.toFile());
    
    //if (image.getType() != BufferedImage.TYPE_INT_ARGB)
    //  throw new IllegalArgumentException("Image should be ARGB");
    
    colorIndices = new TreeMap<>();
  }
  
  public void computeColorIndices()
  {
    colorIndices.clear();
    
    for (int y = 0; y < height(); ++y)
      for (int x = 0; x < width(); ++x)
      {
        int color = image.getRGB(x,y);
                
        if ((color & 0xFF000000) != 0)
          colorIndices.putIfAbsent(color & 0x00FFFFFF, colorIndices.size() + 1);
      }
    
    /*log("Image has %d colors: ", colorIndices.size());
    for (Map.Entry<Integer,Integer> entry : colorIndices.entrySet())
      log("%2d: %s", entry.getValue(), toString(entry.getKey()));*/
  }
  
  public void sortColorIndices()
  {
    List<Color> colors = colorIndices.keySet()
        .stream()
        .filter(c -> c != 0)
        .map(C::c)
        .collect(Collectors.toList());    
    
    Collections.sort(colors, (c1, c2) -> {
      float[] hsv1 = new float[3], hsv2 = new float[3];
      Color.RGBtoHSB(c1.getRed(), c1.getGreen(), c1.getBlue(), hsv1);
      Color.RGBtoHSB(c2.getRed(), c2.getGreen(), c2.getBlue(), hsv2);

      if (hsv1[0] < hsv2[0])
        return -1;
      else if (hsv1[0] > hsv2[0])
        return 1;
      else
      {
        float lum1 = c1.getRed()*0.241f + c1.getBlue()*0.691f + c1.getBlue()*0.068f;
        float lum2 = c2.getRed()*0.241f + c2.getBlue()*0.691f + c2.getBlue()*0.068f;
       
        if (lum1 < lum2)
          return -1;
        else if (lum1 > lum2)
          return 1;
        else
        {
          if (hsv1[2] < hsv2[2])
            return -1;
          else if (hsv1[2] > hsv2[2])
            return 1;
          else
            return 0;      
        }
      }
    });
    
    for (int i = 0; i < colors.size(); ++i)
      colorIndices.put(C.c(colors.get(i)), i+1);
    
  }
  
  public Image image() { return image; }
  
  public int get(int x, int y) { return image.getRGB(x, y); }
  
  public int width() { return image.getWidth(); }
  public int height() { return image.getHeight(); }
  public int colorCount() { return colorIndices.size(); }
  public Map<Integer, Integer> palette() { return colorIndices; }
  
  public String toString(int c)
  {
    return String.format("(%d, %3d, %3d, %3d)", (c & 0xFF000000) >> 24, (c & 0x00FF0000) >> 16, (c & 0xFF00) >> 8, (c & 0xFF));
  }
  
  public void reduceColors(int count)
  {
    OctreeQuantizer quantizer = new OctreeQuantizer();
    
    /* compute octree */
    for (int y = 0; y < height(); ++y)
      for (int x = 0; x < width(); ++x)
      {
        int c = image.getRGB(x, y);
        if ((c & 0xFF000000) != 0)
          quantizer.addColor(c & 0x00FFFFFF);
      }

    /* build palette */
    quantizer.buildPalette(count);
    
    /* apply palette to image */
    Map<Integer, Integer> cache = new HashMap<>();
    for (int originalColor : colorIndices.keySet())
    {
      int mappedColor = quantizer.paletteColorForOriginal(originalColor);
      cache.put(originalColor, mappedColor);
      //System.out.println("Mapping "+toString(originalColor)+" -> "+toString(mappedColor));
    }

    for (int y = 0; y < height(); ++y)
      for (int x = 0; x < width(); ++x)
      {     
        int c = image.getRGB(x,y);  
        
        
        if ((c & 0xFF000000) != 0)
        {
          Integer mc = cache.get(c & 0x00FFFFFF);
        
          if (mc == null) throw new IllegalArgumentException("Mapped color not found: "+toString(c)+" at "+x+", "+y);
        
          image.setRGB(x, y, (c & 0xFF000000) | mc);
        }
      }
    
    /* remap colorIndices */
    colorIndices.clear();
    int[] palette = quantizer.palette();
    for (int i = 0; i < palette.length; ++i)
      colorIndices.put(palette[i], i+1);
  }
  
  public void save(Path path) throws IOException
  {
    ImageIO.write(image, "PNG", path.toFile());
  }
}
