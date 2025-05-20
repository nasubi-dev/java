package oop1.k24083;

import java.awt.Color;
import java.awt.Graphics;

public class Triangle extends Shape {
  private int[] xPoints;
  private int[] yPoints;

  public Triangle(int x1, int y1, int x2, int y2, int x3, int y3, Color color) {
    super(color);
    this.xPoints = new int[] { x1, x2, x3 };
    this.yPoints = new int[] { y1, y2, y3 };
  }

  @Override
  public void draw(Graphics g) {
    g.setColor(color);
    g.fillPolygon(xPoints, yPoints, 3);
  }

  @Override
  public boolean contains(int x, int y) {
    // 三角形の内部判定（重心座標法）
    int x1 = xPoints[0], x2 = xPoints[1], x3 = xPoints[2];
    int y1 = yPoints[0], y2 = yPoints[1], y3 = yPoints[2];

    double denominator = ((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3));
    double a = ((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3)) / denominator;
    double b = ((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3)) / denominator;
    double c = 1 - a - b;

    return a >= 0 && a <= 1 && b >= 0 && b <= 1 && c >= 0 && c <= 1;
  }

  @Override
  public void move(int dx, int dy) {
    for (int i = 0; i < 3; i++) {
      xPoints[i] += dx;
      yPoints[i] += dy;
    }
  }
}
