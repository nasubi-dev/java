package oop1.k24083;

import java.awt.Color;
import java.awt.Graphics;

public class Circle extends Shape {
  private int centerX;
  private int centerY;
  private int radius;

  public Circle(int centerX, int centerY, int radius, Color color) {
    super(color);
    this.centerX = centerX;
    this.centerY = centerY;
    this.radius = radius;
  }

  public int getRadius() {
    return radius;
  }

  public void setRadius(int radius) {
    this.radius = radius;
  }

  @Override
  public void draw(Graphics g) {
    g.setColor(color);
    g.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    if (isSelected) {
      g.setColor(Color.RED);
      g.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }
  }

  @Override
  public boolean contains(int x, int y) {
    double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
    return distance <= radius;
  }

  @Override
  public void move(int dx, int dy) {
    centerX += dx;
    centerY += dy;
  }

  @Override
  public Circle clone() {
    return new Circle(centerX, centerY, radius, color);
  }
}
