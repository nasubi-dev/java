package oop1.k24083;

import java.awt.Color;
import java.awt.Graphics;

public class Shape {
  protected int x; // 図形のX座標
  protected int y; // 図形のY座標
  protected Color color; // 図形の色

  public Shape(int x, int y, Color color) {
    this.x = x;
    this.y = y;
    this.color = color;
  }

  public void draw(Graphics g) {
    System.err.println("具体的な処理内容はサブクラスで実装してください！");
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }
}
