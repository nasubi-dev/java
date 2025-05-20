package oop1.k24083;

import java.awt.Color;
import java.awt.Graphics;

public class Rectangle extends Shape {
  private int x;
  private int y;
  private int width; // 四角形の幅
  private int height; // 四角形の高さ

  public Rectangle(int x, int y, int width, int height, Color color) {
    super(color);
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  @Override
  public void draw(Graphics g) {
    g.setColor(color);
    g.fillRect(x, y, width, height);
    if (isSelected) {
      g.setColor(Color.RED);
      g.drawRect(x, y, width, height);
    }
  }

  @Override
  public boolean contains(int x, int y) {
    return x >= this.x && x <= this.x + width &&
        y >= this.y && y <= this.y + height;
  }

  @Override
  public void move(int dx, int dy) {
    x += dx;
    y += dy;
  }
}
