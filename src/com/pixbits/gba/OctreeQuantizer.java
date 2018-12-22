package com.pixbits.gba;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class OctreeQuantizer
{
  private final int maxDepth;
  private List<List<OctreeNode>> levels;
  private int[] palette;
  OctreeNode root;
  
  public OctreeQuantizer(int maxDepth)
  {
    this.maxDepth = maxDepth;
    this.levels = new ArrayList<>(maxDepth);
    this.root = new OctreeNode(this, 0);
  }
  
  public void addNodeForLevel(int depth, OctreeNode node)
  {
    while (depth >= levels.size())
      levels.add(new ArrayList<>());
    
    levels.get(depth).add(node);
  }
  
  public void addColor(int color)
  {
    root.addColor(color, 0);
  }
  
  public void buildPalette(int count)
  {
    long leavesCount = root.leafNodes().count();
    
    for (int i = maxDepth - 1; i >= 0; --i)
    {
      if (i < levels.size())
      {
        for (OctreeNode node : levels.get(i))
        {
          leavesCount -= node.removeLeaves();
          if (leavesCount < count)
            break;
        }
      }
      
      if (leavesCount < count)
        break;
      
      levels.get(i).clear();
    }
    
    AtomicInteger paletteIndex = new AtomicInteger();
    List<Integer> palette = new ArrayList<>();
    
    root.leafNodes().forEach(node -> {
      if (node.isLeaf())
      {
        palette.add(node.color());
        node.setPaletteIndex(paletteIndex.getAndIncrement());
      } 
    });
    
    this.palette = palette.stream().mapToInt(i -> i).toArray();
  }
  
  public int[] palette() { return palette; }
  public int maxDepth() { return maxDepth; }
}
