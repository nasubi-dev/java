package oop1.k24083;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
  private DrawingPanel drawingPanel; // 描画領域のパネル

  public MainFrame() {
    setTitle("図形描画アプリケーション - K24083");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(800, 600);
    setLocationRelativeTo(null); // 画面中央に配置

    drawingPanel = new DrawingPanel();

    // --- 図形選択ラジオボタン ---
    JRadioButton circleRadioButton = new JRadioButton("円");
    circleRadioButton.setActionCommand("Circle");
    circleRadioButton.setSelected(true); // 最初は円を選択状態にする

    JRadioButton rectangleRadioButton = new JRadioButton("四角形");
    rectangleRadioButton.setActionCommand("Rectangle");

    // ButtonGroupを作成し、ラジオボタンをグループ化する
    ButtonGroup shapeGroup = new ButtonGroup();
    shapeGroup.add(circleRadioButton);
    shapeGroup.add(rectangleRadioButton);

    // ラジオボタン用のアクションリスナー
    ActionListener shapeSelectionListener = e -> {
      // 選択されたラジオボタンのアクションコマンドをDrawingPanelに伝える
      drawingPanel.setCurrentShapeType(e.getActionCommand());
    };

    circleRadioButton.addActionListener(shapeSelectionListener);
    rectangleRadioButton.addActionListener(shapeSelectionListener);
    // --- ここまで図形選択ラジオボタン ---

    // --- 色選択ラジオボタン ---
    JRadioButton redRadioButton = new JRadioButton("赤");
    redRadioButton.setForeground(Color.RED);

    JRadioButton blueRadioButton = new JRadioButton("青");
    blueRadioButton.setForeground(Color.BLUE);

    JRadioButton greenRadioButton = new JRadioButton("緑");
    greenRadioButton.setForeground(Color.GREEN);

    // ButtonGroupで色選択ラジオボタンをグループ化
    ButtonGroup colorGroup = new ButtonGroup();
    colorGroup.add(redRadioButton);
    colorGroup.add(blueRadioButton);
    colorGroup.add(greenRadioButton);

    // 初期選択色を設定 (青)
    blueRadioButton.setSelected(true);

    // 色選択ラジオボタン用のアクションリスナー
    ActionListener colorSelectionListener = e -> {
      if (e.getSource() == redRadioButton) {
        drawingPanel.setCurrentColor(Color.RED);
      } else if (e.getSource() == blueRadioButton) {
        drawingPanel.setCurrentColor(Color.BLUE);
      } else if (e.getSource() == greenRadioButton) {
        drawingPanel.setCurrentColor(Color.GREEN);
      }
    };

    redRadioButton.addActionListener(colorSelectionListener);
    blueRadioButton.addActionListener(colorSelectionListener);
    greenRadioButton.addActionListener(colorSelectionListener);
    // --- ここまで色選択ラジオボタン ---

    // --- クリアボタン ---
    JButton clearButton = new JButton("クリア");
    clearButton.addActionListener(e -> {
      drawingPanel.clearShapes(); // 描画パネルの図形をクリア
    });
    // --- ここまでクリアボタン ---

    // ツールバーにコンポーネントを配置
    JToolBar toolBar = new JToolBar();
    toolBar.add(new JLabel("図形: "));
    toolBar.add(circleRadioButton);
    toolBar.add(rectangleRadioButton);
    toolBar.addSeparator();
    toolBar.add(new JLabel("色: "));
    toolBar.add(redRadioButton);
    toolBar.add(blueRadioButton);
    toolBar.add(greenRadioButton);
    toolBar.addSeparator();
    toolBar.add(clearButton);

    // レイアウトの設定
    add(toolBar, BorderLayout.NORTH);
    add(drawingPanel, BorderLayout.CENTER);

    setVisible(true); // ウィンドウを表示
  }

  public static void main(String[] args) {
    // イベントディスパッチスレッドでGUIを作成
    SwingUtilities.invokeLater(() -> new MainFrame());
  }
}
