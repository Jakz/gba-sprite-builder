package com.pixbits.gba;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

public class ImageTransferHandler extends TransferHandler
{
  private static final DataFlavor FILE_FLAVOR = DataFlavor.javaFileListFlavor;

  private final Consumer<File> acceptor;

  public ImageTransferHandler(Consumer<File> acceptor)
  {
    this.acceptor = acceptor;
  }

  @SuppressWarnings("unchecked")
  public boolean importData(JComponent c, Transferable t)
  {
    if (canImport(c, t.getTransferDataFlavors())) 
    {
      if (transferFlavor(t.getTransferDataFlavors(), FILE_FLAVOR)) 
      {
        try 
        {
          List<File> fileList = (List<File>) t.getTransferData(FILE_FLAVOR);
          fileList.forEach(f -> acceptor.accept(f));        
          return true;
        } 
        catch (IOException|UnsupportedFlavorException e) 
        {
          e.printStackTrace();
        } 
      }
    }
    return false;
  }

  public int getSourceActions(JComponent c) { return COPY; }

  protected void exportDone(JComponent c, Transferable data, int action)
  {
    c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  private boolean transferFlavor(DataFlavor[] flavors, DataFlavor flavor)
  {
    boolean found = false;
    for (int i = 0; i < flavors.length && !found; i++) {
      found = flavors[i].equals(flavor);
    }
    return found;
  }


  public boolean canImport(JComponent c, DataFlavor[] flavors)
  {    
    for (int i = 0; i < flavors.length; i++)
    {
      if (FILE_FLAVOR.equals(flavors[i]))
      {
        return true;
      }
    }
    return false;
  }
}