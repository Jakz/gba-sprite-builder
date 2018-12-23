package com.pixbits.gba;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;

import javax.imageio.ImageIO;

public class SourceImage
{
  private final BufferedImage image;
  private final Map<Integer, Integer> colorIndices;
  
  public SourceImage(Path path) throws IOException
  {
    image = ImageIO.read(path.toFile());
    
    //if (image.getType() != BufferedImage.TYPE_INT_ARGB)
    //  throw new IllegalArgumentException("Image should be ARGB");
    
    colorIndices = new HashMap<>();
  }
  
  public void computeColorIndices()
  {
    colorIndices.clear();
    
    for (int y = 0; y < height(); ++y)
      for (int x = 0; x < width(); ++x)
      {
        int color = image.getRGB(x,y);
        if ((color & 0xFF000000) == 0xFF000000)
          colorIndices.putIfAbsent(color & 0x00FFFFFF, colorIndices.size() + 1);
      }
  }
  
  public Image image() { return image; }
  
  public int get(int x, int y) { return image.getRGB(x, y); }
  
  public int width() { return image.getWidth(); }
  public int height() { return image.getHeight(); }
  public int colorCount() { return colorIndices.size(); }
  public Map<Integer, Integer> palette() { return colorIndices; }
  
  public void reduceColors(int count)
  {
    OctreeQuantizer quantizer = new OctreeQuantizer();
    
    /* compute octree */
    for (int y = 0; y < height(); ++y)
      for (int x = 0; x < width(); ++x)
        quantizer.addColor(image.getRGB(x,y) & 0x00FFFFFF);

    /* build palette */
    quantizer.buildPalette(count);
    
    /* apply palette to image */
    Map<Integer, Integer> cache = new HashMap<>();
    for (int originalColor : colorIndices.keySet())
    {
      int mappedColor = quantizer.paletteColorForOriginal(originalColor);
      cache.put(originalColor, mappedColor);
    }

    for (int y = 0; y < height(); ++y)
      for (int x = 0; x < width(); ++x)
      {
        int c = image.getRGB(x,y);
        image.setRGB(x, y, (c & 0xFF000000) | cache.get(c & 0x00FFFFFF));
      }
    
    /* remap colorIndices */
    colorIndices.clear();
    int[] palette = quantizer.palette();
    for (int i = 0; i < palette.length; ++i)
      colorIndices.put(palette[i], i);
  }
  
  public void save(Path path) throws IOException
  {
    ImageIO.write(image, "PNG", path.toFile());
  }
}
