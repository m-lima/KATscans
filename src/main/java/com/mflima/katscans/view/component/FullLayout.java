package com.mflima.katscans.view.component;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;

/**
 * @author Marcelo Lima
 */
public class FullLayout implements LayoutManager2 {

  @Override
  public void addLayoutComponent(Component comp, Object constraints) {
  }

  @Override
  public void addLayoutComponent(String name, Component comp) {
  }

  @Override
  public void removeLayoutComponent(Component comp) {
  }

  @Override
  public Dimension maximumLayoutSize(Container target) {
    return target.getMaximumSize();
  }

  @Override
  public float getLayoutAlignmentX(Container target) {
    return 0.5f;
  }

  @Override
  public float getLayoutAlignmentY(Container target) {
    return 0.5f;
  }

  @Override
  public void invalidateLayout(Container target) {
  }

  @Override
  public Dimension preferredLayoutSize(Container parent) {
    return parent.getPreferredSize();
  }

  @Override
  public Dimension minimumLayoutSize(Container parent) {
    return parent.getMinimumSize();
  }

  @Override
  public void layoutContainer(Container parent) {
    synchronized (parent.getTreeLock()) {
      Insets insets = parent.getInsets();
      Rectangle bounds = parent.getBounds();
      int top = insets.top;
      int bottom = bounds.height - insets.bottom;
      int left = insets.left;
      int right = bounds.width - insets.right;

      Component[] components = parent.getComponents();
      for (Component component : components) {
        component.setBounds(left, top, right, bottom);
      }
    }
  }

}
