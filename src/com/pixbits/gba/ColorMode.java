package com.pixbits.gba;

public enum ColorMode
{
  INDEXED_BPP1("2 colors, 1bpp", 2),
  INDEXED_BPP2("4 colors, 2bpp", 4),
  INDEXED_BPP4("16 colors, 4bpp", 16),
  INDEXED_BPP8("256 colors, 8bpp", 256),
  RGB555("32768 colors, rgb555", -1)
  ;
  
  public final String caption;
  public final int colorCount;
  
  private ColorMode(String caption, int colorCount)
  {
    this.caption = caption;
    this.colorCount = colorCount;
  }
  
  public String toString() { return caption; }
}
