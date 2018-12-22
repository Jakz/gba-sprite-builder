package com.pixbits.gba;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class OctreeNode
{
  private final OctreeQuantizer parent;
  
  private final OctreeNode[] children;
  
  private int count;
  private int red, green, blue;
  private int paletteIndex;

  
  OctreeNode(OctreeQuantizer parent, int depth)
  {
    this.parent = parent;
    children = new OctreeNode[8];
    
    if (depth < parent.maxDepth())
      parent.addNodeForLevel(depth, this);
  }
  
  private int indexForColor(int color, int depth)
  {
    int mask = 0x80 >> depth; 
    
    int index = 
         ((red(color) & mask) != 0 ? 1 : 0) |
         ((green(color) & mask) != 0 ? 2 : 0) |
         ((blue(color) & mask) != 0 ? 4 : 0);
     return index;
  }
  
  public boolean isLeaf() { return count > 0; }
  public Stream<OctreeNode> leafNodes() { 
    return isLeaf() ? 
        Stream.of(this) : 
          Arrays.stream(children).filter(Objects::nonNull).flatMap(OctreeNode::leafNodes); 
  }
  
  public int totalCount() { return leafNodes().mapToInt(n -> n.count).sum(); }
  
  void addColor(int color, int depth)
  {
    if (depth >= parent.maxDepth())
    {
      red += red(color);
      green += green(color);
      blue += blue(color);
      count += 1;
    }
    else
    {
      int index = indexForColor(color, depth);
      
      if (children[index] == null)
        children[index] = new OctreeNode(parent, depth);
      
      children[index].addColor(color, depth+1);
    }
  }
  
  public int paletteIndex(int depth)
  {
    if (isLeaf())
      return paletteIndex;
    else
    {
      //TODO: optimize
      int index = indexForColor(color(red, green, blue), depth);
      
      if (children[index] != null)
        return children[index].paletteIndex(depth + 1);
      else
        return Arrays.stream(children).mapToInt(n -> n.paletteIndex(depth + 1)).findFirst().orElse(0);
    }
  }
  
  public int removeLeaves()
  {
    int removed = 0;
    
    for (OctreeNode node : children)
    {
      if (node != null)
      {
        red += node.red;
        green += node.green;
        blue += node.blue;
        count += node.count;
        ++removed;
      }
    }
    
    return removed;
  }
  
  public void setPaletteIndex(int index) { this.paletteIndex = index; }
  public int color() { return color(red / count, green / count, blue / count); }
  
  private int red(int color) { return (color >> 16) & 0xFF; }
  private int green(int color) { return (color >> 8) & 0xFF; }
  private int blue(int color) { return (color >> 0) & 0xFF; }
  private int color(int r, int g, int b) { return (r << 16) | (g << 8) | b; }
}