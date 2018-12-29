package com.pixbits.gba;

import java.awt.Color;

public class C
{
  public static int a(int c) { return (c >> 24) & 0xFF; }
  public static int r(int c) { return (c >> 16) & 0xFF; }
  public static int g(int c) { return (c >> 8) & 0xFF; }
  public static int b(int c) { return c & 0xFF; }
  
  public static Color c(int c) { return new Color(r(c), g(c), b(c)); } 
  public static int c(int r, int g, int b) { return (r << 16) | (g << 8) | b; } 
  public static int c(Color c) { return (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue(); } 
}
