package oop1.k24083;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Stack;

public class DrawingPanel extends JPanel {
  private Shape[] shapes; // 描画されたShapeオブジェクトの配列
  private String currentShapeType; // 現在選択されている描画する図形の種類
  private Color currentColor; // 現在選択されている描画色
  private Point startPoint; // ドラッグ開始位置
  private Shape previewShape; // プレビュー用の図形
  private Shape selectedShape; // 選択された図形
  private Point lastPoint; // 最後のマウス位置
  private boolean isDragging; // ドラッグ中かどうか

  // Undo/Redo用のスタック
  private Stack<Shape[]> undoStack = new Stack<>();
  private Stack<Shape[]> redoStack = new Stack<>();
  private Shape[] lastState; // 操作前の状態を一時保存

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

          // 図形移動の前に状態を保存
          saveState();
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
          saveState(); // 操作前の状態を保存
          addShape(previewShape);
          startPoint = null;
          previewShape = null;
        } else if (isDragging && selectedShape != null) {
          // 図形移動操作の履歴を保存
          pushToUndoStack();
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
        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_Z && e.isControlDown()) {
          undo();
        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_Y && e.isControlDown()) {
          redo();
        }
      }
    });

    addMouseListener(mouseHandler);
    addMouseMotionListener(mouseHandler);
  }

  // 状態を保存するメソッド（操作前に呼び出す）
  private void saveState() {
    // 現在の図形配列をディープコピー
    lastState = new Shape[shapes.length];
    for (int i = 0; i < shapes.length; i++) {
      lastState[i] = shapes[i].clone();
    }
  }

  // 操作履歴をUndoスタックに保存（操作後に呼び出す）
  private void pushToUndoStack() {
    if (lastState != null) {
      undoStack.push(lastState);
      redoStack.clear(); // 新しい操作が行われたらRedoスタックをクリア
      lastState = null;
    }
  }

  // Undo操作を行うメソッド
  public boolean undo() {
    if (undoStack.isEmpty()) {
      return false; // Undo不可能
    }

    // 現在の状態をRedoスタックに保存
    Shape[] currentState = new Shape[shapes.length];
    for (int i = 0; i < shapes.length; i++) {
      currentState[i] = shapes[i].clone();
    }
    redoStack.push(currentState);

    // 以前の状態を復元
    shapes = undoStack.pop();

    // 選択状態をリセット
    selectedShape = null;

    repaint();
    return true;
  }

  // Redo操作を行うメソッド
  public boolean redo() {
    if (redoStack.isEmpty()) {
      return false; // Redo不可能
    }

    // 現在の状態をUndoスタックに保存
    Shape[] currentState = new Shape[shapes.length];
    for (int i = 0; i < shapes.length; i++) {
      currentState[i] = shapes[i].clone();
    }
    undoStack.push(currentState);

    // Redoスタックから状態を復元
    shapes = redoStack.pop();

    // 選択状態をリセット
    selectedShape = null;

    repaint();
    return true;
  }

  private void deleteSelectedShape() {
    // 操作前の状態を保存
    saveState();

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

    // 操作履歴を保存
    pushToUndoStack();

    repaint();
  }

  public void addShape(Shape shape) {
    // 操作前の状態を保存
    saveState();

    // 配列を1つ大きくしてshapeを追加
    Shape[] newShapes = new Shape[this.shapes.length + 1];
    for (int i = 0; i < this.shapes.length; i++) {
      newShapes[i] = this.shapes[i];
    }
    newShapes[newShapes.length - 1] = shape;
    this.shapes = newShapes;

    // 操作履歴を保存
    pushToUndoStack();
  }

  public void clearShapes() {
    // 操作前の状態を保存
    saveState();

    this.shapes = new Shape[0]; // 空の配列で上書き

    // 操作履歴を保存
    pushToUndoStack();

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
