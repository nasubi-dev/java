import javax.swing.*;
import java.awt.*;

public class FizzBazzApp extends JFrame {

    private JTextField inputField; // 文字を入力するフィールド
    private JButton processButton; // 処理を実行するボタン
    private JTextArea outputArea; // 処理結果を表示するエリア

    public FizzBazzApp() {
        // --- ウィンドウの基本設定 ---
        setTitle("K24083:FizzBuzz");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);

        // --- レイアウトにBorderLayoutを採用 ---
        // 部品間の隙間を縦横5ピクセルに設定
        setLayout(new BorderLayout(5, 5));

        // --- 上部に配置する部品 (入力欄、ボタンなど) ---
        // これらの部品をまとめるためのパネルを作成 (FlowLayoutを使用)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel inputLabel = new JLabel("最大値：");
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
            outputArea.setText("");
            int input = Integer.parseInt(inputText);
            for (int i = 1; i <= input; i++) {
                if (i % 3 == 0 && i % 5 == 0) {
                    outputArea.append("FizzBuzz" + System.lineSeparator());
                } else if (i % 3 == 0) {
                    outputArea.append("Fizz" + System.lineSeparator());
                } else if (i % 5 == 0) {
                    outputArea.append("Buzz" + System.lineSeparator());
                } else {
                    outputArea.append(i + System.lineSeparator());
                }
            }
        });

        // --- ウィンドウを表示 ---
        setVisible(true);
    }

    public static void main(String[] args) {
        // イベントディスパッチスレッドでGUIを作成・実行
        SwingUtilities.invokeLater(() -> new FizzBazzApp());
    }
}