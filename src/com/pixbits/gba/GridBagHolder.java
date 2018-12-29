package com.pixbits.gba;

import java.awt.GridBagConstraints;
import java.awt.Insets;

class GridBagHolder
{
  private GridBagConstraints c;
  
  GridBagHolder() { c = new GridBagConstraints(); }
  
  public GridBagHolder w(float x, float y) { c.weightx = x; c.weighty = y; return this; }
  public GridBagHolder g(int x, int y) { c.gridx = x; c.gridy = y; return this; }
  
  public GridBagHolder x(int x) { c.gridx = x; return this; }
  public GridBagHolder nx() { c.gridx = GridBagConstraints.RELATIVE; return this; }
  public GridBagHolder lx() { c.gridx = GridBagConstraints.REMAINDER; return this; }

  public GridBagHolder y(int y) { c.gridy = y; return this; }
  public GridBagHolder ny() { c.gridy = GridBagConstraints.RELATIVE; return this; }
  public GridBagHolder ly() { c.gridy = GridBagConstraints.REMAINDER; return this; }
  
  public GridBagHolder xy(int x, int y) { return x(x).y(y); }

  
  public GridBagHolder nxly() { return nx().ly(); }
  public GridBagHolder lxny() { return lx().ny(); }

  
  public GridBagHolder w(int w) { c.gridwidth = w; return this; }
  public GridBagHolder h(int h) { c.gridheight = h; return this; }
  public GridBagHolder wh(int w, int h) { return w(w).h(h); }
  public GridBagHolder a(int a) { c.anchor = a; return this; }
  
  public GridBagHolder p(int p) { c.ipadx = p; c.ipady = p; return this; }
  public GridBagHolder py(int py) { c.ipady = py; return this; }
  
  public GridBagHolder leftInsets(int v) { c.insets = new Insets(0, v, 0, 0); return this; }
  public GridBagHolder rightInsets(int v) { c.insets = new Insets(0, 0, 0, v); return this; }
  public GridBagHolder bottomInsets(int v) { c.insets = new Insets(0, 0, v, 0); return this; }
  public GridBagHolder hInsets(int v) { c.insets = new Insets(0, v, 0, v); return this; }
  public GridBagHolder insets(int v) { c.insets = new Insets(v, v, v, v); return this; }
  public GridBagHolder noInsets() { c.insets = new Insets(0,0,0,0); return this; }
  
  public GridBagHolder i(int v) { return insets(v); }
  
  public GridBagHolder noFill() { c.fill = GridBagConstraints.NONE; return this; }
  public GridBagHolder fill() { c.fill = GridBagConstraints.BOTH; return this; }
  public GridBagHolder vfill() { c.fill = GridBagConstraints.VERTICAL; return this; }
  public GridBagHolder hfill() { c.fill = GridBagConstraints.HORIZONTAL; return this; }
  
  public GridBagHolder a(String a)
  {    
    switch (a)
    {
      case "tl": return a(GridBagConstraints.FIRST_LINE_START);
      case "tc": return a(GridBagConstraints.PAGE_START);
      case "tr": return a(GridBagConstraints.FIRST_LINE_END);
      
      case "cl": return a(GridBagConstraints.LINE_START);
      case "cc": return a(GridBagConstraints.CENTER);
      case "cr": return a(GridBagConstraints.LINE_END);
      
      case "bl": return a(GridBagConstraints.LAST_LINE_START);
      case "bc": return a(GridBagConstraints.PAGE_END);
      case "br": return a(GridBagConstraints.LAST_LINE_END);
      
      default:
        throw new IllegalArgumentException("unrecognized anchor string: "+a);
    }
  }
  
  public GridBagHolder lineEnd() { return a(GridBagConstraints.LINE_END); }
  public GridBagHolder lineStart() { return a(GridBagConstraints.LINE_START); }
  public GridBagHolder center() { return a(GridBagConstraints.CENTER); }
  
  public GridBagHolder top() { return a(GridBagConstraints.LINE_START); }
  public GridBagHolder topLeft() { return a(GridBagConstraints.FIRST_LINE_START); }
  public GridBagHolder left() { return lineStart(); }

  
  public GridBagConstraints c() { return c; }
}