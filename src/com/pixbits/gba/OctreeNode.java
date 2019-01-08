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
    
    if (depth < OctreeQuantizer.MAX_DEPTH)
      parent.addNodeForLevel(depth, this);
  }
  
  private int indexForColor(int color, int depth)
  {
    int mask = 0x80 >> depth; 
    
    int index = 
         ((C.r(color) & mask) != 0 ? 1 : 0) |
         ((C.g(color) & mask) != 0 ? 2 : 0) |
         ((C.b(color) & mask) != 0 ? 4 : 0);
     return index;
  }
  
  public boolean isLeaf() { return count > 0; }
  public Stream<OctreeNode> leafNodes() { 
    return isLeaf() ? 
        Stream.of(this) : 
          Arrays.stream(children).filter(Objects::nonNull).flatMap(OctreeNode::leafNodes); 
  }
  
  public long colorCount() { return leafNodes().count(); }
  public int pixelCount() { return leafNodes().mapToInt(n -> n.count).sum(); }
  
  void addColor(int color, int depth)
  {
    if (depth >= OctreeQuantizer.MAX_DEPTH)
    {
      red += C.r(color);
      green += C.g(color);
      blue += C.b(color);
      count += 1;
    }
    else
    {
      int index = indexForColor(color, depth);
      
      if (children[index] == null)
        children[index] = new OctreeNode(parent, depth+1);
      
      children[index].addColor(color, depth+1);
    }
  }
  
  public int paletteIndex(int color, int depth)
  {
    if (isLeaf())
      return paletteIndex;
    else
    {
      //TODO: optimize
      int index = indexForColor(color, depth);
      
      if (children[index] != null)
        return children[index].paletteIndex(color, depth + 1);
      else
        return Arrays.stream(children).filter(Objects::nonNull).mapToInt(n -> n.paletteIndex(color, depth + 1)).findFirst().orElse(0);
    }
  }
  
  public int mergeLeaves(int max)
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
      
      if (removed == max)
        break;
    }
    
    if (removed > 0)
      --removed;
    
    //Arrays.fill(children, null);

    return removed;
  }
  
  public void setPaletteIndex(int index) { this.paletteIndex = index; }
  public int color() { return C.c(red / count, green / count, blue / count); }
  
  public String toString() { return count > 0 ? String.format("color: (%d,%d,%d), count: %d, index: %d", red/count, green/count, blue/count, count, paletteIndex) : "node"; }
  
  public void print(StringBuilder builder, int depth, String prefix, boolean stopAtLeaves)
  {
    builder.append(" ".repeat(depth*2)).append(prefix).append(toString()).append("\n");
    
    if (!isLeaf() || !stopAtLeaves)
      for (int i = 0; i < 8; ++i)
        if (children[i] != null)
          children[i].print(builder, depth+1, Integer.toString(i) + ": ", stopAtLeaves);
  }
  
}
