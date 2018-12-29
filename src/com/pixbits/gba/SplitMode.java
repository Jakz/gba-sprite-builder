package com.pixbits.gba;

public class SplitMode
{
  public class SpriteCoord
  {
    private final SplitMode mode;
    public final int x, y;
    
    private SpriteCoord(SplitMode mode, int x, int y)
    {
      this.x = x;
      this.y = y;
      this.mode = mode;
    }

    
    private SpriteCoord(SplitMode mode)
    {
      this(mode, 0, 0);
    }
    
    public SpriteCoord next()
    {
      if (mode.hor)
      {
        if (x < mode.cw - 1)
          return new SpriteCoord(mode, x+w, y);
        else if (y < mode.ch - 1)
          return new SpriteCoord(mode, 0, y+h);
        else
          return null;
      }
      else
      {
        if (y < mode.ch - 1)
          return new SpriteCoord(mode, x, y+h);
        else if (x < mode.cw - 1)
          return new SpriteCoord(mode, x+w, 0);
        else
          return null;
      }
    }
    
    public int width() { return mode.w; }
    public int height() { return mode.h; }
  };
  
  private int w, h;
  private int cw, ch;
  private boolean hor;
  
  public SplitMode(int w, int h, boolean hor, int cw, int ch)
  {
    this.w = w;
    this.h = h;
    this.hor = hor;
    this.cw = cw;
    this.ch = ch;
  }
  
  public String toString() { return String.format("%d sprites %dx%d%s", cw*ch, w*8, h*8, cw==1 && ch==1 ? "" : (hor ? " →" : " ↓")); }
  
  int tilesWide() { return w; }
  int tilesHigh() { return h; }
  
  int spriteCount() { return cw*ch; }
  
  SpriteCoord iterator() { return new SpriteCoord(this); }
}
