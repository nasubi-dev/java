package oop1.k24083;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DrawingPanel extends JPanel {
  private Shape[] shapes; // 描画されたShapeオブジェクトの配列
  private String currentShapeType; // 現在選択されている描画する図形の種類
  private Color currentColor; // 現在選択されている描画色
  private Point startPoint; // ドラッグ開始位置
  private Shape previewShape; // プレビュー用の図形
  private Shape selectedShape; // 選択された図形
  private Point lastPoint; // 最後のマウス位置
  private boolean isDragging; // ドラッグ中かどうか

  public DrawingPanel() {
    shapes = new Shape[0]; // 空の配列で初期化
    currentShapeType = "Circle"; // デフォルトは円
    currentColor = Color.BLUE; // デフォルトは青
    setBackground(Color.WHITE); // 背景色を白に設定

    MouseAdapter mouseHandler = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        startPoint = e.getPoint();
        lastPoint = e.getPoint();
        previewShape = null;

        // 図形の選択
        Shape clickedShape = null;
        for (Shape shape : shapes) {
          if (shape.contains(e.getX(), e.getY())) {
            clickedShape = shape;
          }
        }

        // 選択状態を更新
        if (clickedShape != null) {
          if (selectedShape != null) {
            selectedShape.setSelected(false);
          }
          selectedShape = clickedShape;
          selectedShape.setSelected(true);
          isDragging = true;
        } else {
          if (selectedShape != null) {
            selectedShape.setSelected(false);
            selectedShape = null;
          }
          isDragging = false;
        }
        repaint();
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        if (isDragging && selectedShape != null) {
          // 選択された図形を移動
          int dx = e.getX() - lastPoint.x;
          int dy = e.getY() - lastPoint.y;
          selectedShape.move(dx, dy);
          lastPoint = e.getPoint();
        } else if (startPoint != null) {
          // 新しい図形をプレビュー
          int width = e.getX() - startPoint.x;
          int height = e.getY() - startPoint.y;

          switch (currentShapeType) {
            case "Circle":
              int diameter = Math.max(Math.abs(width), Math.abs(height));
              previewShape = new Circle(startPoint.x, startPoint.y, diameter / 2, currentColor);
              break;
            case "Rectangle":
              previewShape = new Rectangle(
                  Math.min(startPoint.x, e.getX()),
                  Math.min(startPoint.y, e.getY()),
                  Math.abs(width),
                  Math.abs(height),
                  currentColor);
              break;
            case "Triangle":
              // 三角形は3点で定義：開始点、現在点、その中間点を使用
              int midX = startPoint.x;
              int midY = e.getY();
              previewShape = new Triangle(
                  startPoint.x, startPoint.y,
                  e.getX(), e.getY(),
                  midX, midY,
                  currentColor);
              break;
          }
          repaint();
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (!isDragging && startPoint != null && previewShape != null) {
          addShape(previewShape);
          startPoint = null;
          previewShape = null;
        }
        isDragging = false;
        repaint();
      }
    };

    // キーボードイベントのリスナーを追加
    setFocusable(true);
    addKeyListener(new java.awt.event.KeyAdapter() {
      @Override
      public void keyPressed(java.awt.event.KeyEvent e) {
        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DELETE && selectedShape != null) {
          deleteSelectedShape();
        }
      }
    });

    addMouseListener(mouseHandler);
    addMouseMotionListener(mouseHandler);
  }

  private void deleteSelectedShape() {
    // 選択された図形を配列から削除
    Shape[] newShapes = new Shape[shapes.length - 1];
    int index = 0;
    for (Shape shape : shapes) {
      if (shape != selectedShape) {
        newShapes[index++] = shape;
      }
    }
    shapes = newShapes;
    selectedShape = null;
    repaint();
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

    // プレビュー用の図形を描画
    if (previewShape != null) {
      previewShape.draw(g);
    }
  }
}
