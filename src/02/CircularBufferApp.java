import javax.swing.*;
import java.awt.*;

public class CircularBufferApp extends JFrame {
  public String[] buffer = new String[10];
  private JTextField inputField; // 文字を入力するフィールド
  private JButton processButton; // 処理を実行するボタン
  private JTextArea outputArea; // 処理結果を表示するエリア

  public CircularBufferApp() {
    // --- ウィンドウの基本設定 ---
    setTitle("K24083:サーキュラーバッファ");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(400, 350);
    setLocationRelativeTo(null);

    // --- レイアウトにBorderLayoutを採用 ---
    // 部品間の隙間を縦横5ピクセルに設定
    setLayout(new BorderLayout(5, 5));

    // --- 上部に配置する部品 (入力欄、ボタンなど) ---
    // これらの部品をまとめるためのパネルを作成 (FlowLayoutを使用)
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    JLabel inputLabel = new JLabel("データを入力：");
    inputField = new JTextField(15);
    processButton = new JButton("追加");

    // パネルに部品を追加
    topPanel.add(inputLabel);
    topPanel.add(inputField);
    topPanel.add(processButton);

    // --- 中央に配置する部品 (結果表示エリア) ---
    outputArea = new JTextArea(); // 初期サイズはBorderLayoutが調整
    outputArea.setEditable(false); // 編集不可に設定
    // テキストエリアをスクロール可能にする (JScrollPaneでラップ)
    JScrollPane scrollPane = new JScrollPane(outputArea);

    // --- 部品をウィンドウに追加 ---
    // 上部パネルをウィンドウの北 (上) に配置
    add(topPanel, BorderLayout.NORTH);
    // スクロール可能なテキストエリアをウィンドウの中央に配置（中央領域は利用可能な残りのスペースをすべて使う）
    add(scrollPane, BorderLayout.CENTER);

    // --- ボタンのアクション設定 ---
    processButton.addActionListener(e -> {
      String inputText = inputField.getText();
      outputArea.setText(""); // クリアしてから入力されたテキストを表示

      // CircularBufferAppの実装
      // バッファの内容を1つずつ後ろにずらす
      for (int i = buffer.length - 1; i > 0; i--) {
        buffer[i] = buffer[i - 1];
      }
      // 新しいデータをバッファの先頭に追加
      buffer[0] = inputText;

      // バッファの内容を表示
      for (String data : buffer) {
        if (data != null) {
          outputArea.append(data + System.lineSeparator());
        }
      }
    });

    // --- ウィンドウを表示 ---
    setVisible(true);
  }

  public static void main(String[] args) {
    // イベントディスパッチスレッドでGUIを作成・実行
    SwingUtilities.invokeLater(() -> new CircularBufferApp());
  }
};