package oop1.k24083;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DrawingPanel extends JPanel {
  private Shape[] shapes; // 描画されたShapeオブジェクトの配列
  private String currentShapeType; // 現在選択されている描画する図形の種類
  private Color currentColor; // 現在選択されている描画色

  public DrawingPanel() {
    shapes = new Shape[0]; // 空の配列で初期化
    currentShapeType = "Circle"; // デフォルトは円
    currentColor = Color.BLUE; // デフォルトは青
    setBackground(Color.WHITE); // 背景色を白に設定

    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        Shape newShape = null;

        // currentShapeTypeに応じて適切な図形オブジェクトを生成
        if ("Circle".equals(currentShapeType)) {
          newShape = new Circle(e.getX(), e.getY(), 30, currentColor);
        } else if ("Rectangle".equals(currentShapeType)) {
          // 四角形の場合は、クリック位置を左上の座標として、幅と高さを指定
          newShape = new Rectangle(e.getX() - 30, e.getY() - 30, 60, 60, currentColor);
        }

        if (newShape != null) {
          addShape(newShape);
          repaint(); // パネルを再描画
        }
      }
    });
  }

  public void addShape(Shape shape) {
    // 配列を1つ大きくしてshapeを追加
    Shape[] newShapes = new Shape[this.shapes.length + 1];
    for (int i = 0; i < this.shapes.length; i++) {
      newShapes[i] = this.shapes[i];
    }
    newShapes[newShapes.length - 1] = shape;
    this.shapes = newShapes;
  }

  public void clearShapes() {
    this.shapes = new Shape[0]; // 空の配列で上書き
    repaint(); // パネルを再描画
  }

  public void setCurrentShapeType(String shapeType) {
    this.currentShapeType = shapeType;
  }

  public void setCurrentColor(Color color) {
    this.currentColor = color;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g); // 親クラスの描画処理（背景のクリアなど）

    // shapes配列内のすべての図形を描画
    for (Shape shape : shapes) {
      shape.draw(g);
    }
  }
}
