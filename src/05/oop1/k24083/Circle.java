package oop1.k24083;

import java.awt.Color;
import java.awt.Graphics;

public class Circle extends Shape {
  private int radius; // 円の半径

  public Circle(int x, int y, int radius, Color color) {
    super(x, y, color);
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
    // 円を描画（x, yは中心座標なので、描画位置を調整）
    g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
  }
}
