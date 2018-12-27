package com.pixbits.gba;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class OctreeQuantizer
{
  private List<List<OctreeNode>> levels;
  private int[] palette;
  OctreeNode root;
    
  public static final int MAX_DEPTH = 8;
  
  public OctreeQuantizer()
  {
    this.levels = new ArrayList<>();
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
  
  public OctreeNode root() { return root; }
  
  public int paletteColorForOriginal(int color)
  {
    return palette[root.paletteIndex(color, 0)];
  }
  
  
  public void buildPalette(final int count)
  {
    long leavesCount = root.leafNodes().count();
    
    for (int i = MAX_DEPTH - 1; i >= 0; --i)
    {
      System.out.print("Merging level "+i+", colors before: "+leavesCount);
      for (OctreeNode node : levels.get(i))
      {
        if (!node.isLeaf())
        {
          leavesCount -= node.mergeLeaves();
          if (leavesCount <= count)
            break;
        }
      }
      
      if (leavesCount <= count)
        break;
      
      levels.get(i).clear(); 
      
      System.out.println(" after: "+leavesCount);
    }
    
    System.out.println("\nFinished merging to: "+leavesCount+" ("+root.leafNodes().count()+")");

    
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
}
