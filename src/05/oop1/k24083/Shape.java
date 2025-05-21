package oop1.k24083;

import java.awt.Color;
import java.awt.Graphics;

public abstract class Shape implements Cloneable {
  protected Color color;
  protected boolean isSelected;

  public Shape(Color color) {
    this.color = color;
    this.isSelected = false;
  }

  public abstract void draw(Graphics g);

  public abstract boolean contains(int x, int y);

  public abstract void move(int dx, int dy);

  public void setSelected(boolean selected) {
    this.isSelected = selected;
  }

  public boolean isSelected() {
    return isSelected;
  }

  @Override
  public abstract Shape clone();
}
