import javax.swing.*;
import java.awt.*;

public class NumArrayStatsApp extends JFrame {

  private JTextField inputField; // 文字を入力するフィールド
  private JButton processButton; // 処理を実行するボタン
  private JTextArea outputArea; // 処理結果を表示するエリア

  public NumArrayStatsApp() {
    // --- ウィンドウの基本設定 ---
    setTitle("K20083 統計情報産出");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(400, 350);
    setLocationRelativeTo(null);

    // --- レイアウトにBorderLayoutを採用 ---
    // 部品間の隙間を縦横5ピクセルに設定
    setLayout(new BorderLayout(5, 5));

    // --- 上部に配置する部品 (入力欄、ボタンなど) ---
    // これらの部品をまとめるためのパネルを作成 (FlowLayoutを使用)
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    JLabel inputLabel = new JLabel("データの入力:");
    inputField = new JTextField(15);
    processButton = new JButton("処理実行");

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
      // 入力されたテキストを改行付きで追加
      String[] data = inputText.split(",");
      int[] numbers = new int[data.length];
      // 合計
      int sum = 0;
      for (int i = 0; i < data.length; i++) {
        numbers[i] = Integer.parseInt(data[i].trim());
        sum += numbers[i];
      }
      outputArea.append("合計：" + sum + System.lineSeparator());
      // 平均
      double average = 0.0;
      if (numbers.length > 0) {
        average = (double) sum / numbers.length;
      }
      outputArea.append("平均：" + average + System.lineSeparator());
      // 最小値
      var min = Integer.MAX_VALUE;
      for (int number : numbers) {
        if (number < min) {
          min = number;
        }
      }
      outputArea.append("最小値：" + min + System.lineSeparator());
      // 最大値
      int max = Integer.MIN_VALUE;
      for (int number : numbers) {
        if (number > max) {
          max = number;
        }
      }
      outputArea.append("最大値：" + max + System.lineSeparator());
      // 中央値
      double median = 0.0;
      // 配列をソート
      java.util.Arrays.sort(numbers);
      if (numbers.length % 2 == 0) {
        median = (numbers[numbers.length / 2 - 1] + numbers[numbers.length / 2]) / 2.0;
      } else {
        median = numbers[numbers.length / 2];
      }
      outputArea.append("中央値：" + median + System.lineSeparator());
      // 最頻値
      int[] modes = new int[numbers.length];
      int maxCount = 0;
      for (int i = 0; i < numbers.length; i++) {
        int count = 0;
        for (int j = 0; j < numbers.length; j++) {
          if (numbers[j] == numbers[i]) {
            count++;
          }
        }
        if (count > maxCount) {
          maxCount = count;
          modes[0] = numbers[i];
        } else if (count == maxCount) {
          // すでに最頻値に含まれている場合はスキップ
          boolean alreadyExists = false;
          for (int mode : modes) {
            if (mode == numbers[i]) {
              alreadyExists = true;
              break;
            }
          }
          if (!alreadyExists) {
            for (int k = 0; k < modes.length; k++) {
              if (modes[k] == 0) {
                modes[k] = numbers[i];
                break;
              }
            }
          }
        }
      }
      StringBuilder modeString = new StringBuilder();
      for (int mode : modes) {
        if (mode != 0) {
          if (modeString.length() > 0) {
            modeString.append(",");
          }
          modeString.append(mode);
        }
      }
      outputArea.append("最頻値：[" + modeString.toString().trim() + "]" + System.lineSeparator());
    });

    // --- ウィンドウを表示 ---
    setVisible(true);
  }

  public static void main(String[] args) {
    // イベントディスパッチスレッドでGUIを作成・実行
    SwingUtilities.invokeLater(() -> new NumArrayStatsApp());
  }
}