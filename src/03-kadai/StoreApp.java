import javax.swing.*;
import java.awt.*;

public class StoreApp extends JFrame {

    private JTextField productJanCodeField; // 商品名を入力するフィールド
    private JTextField unitPriceField; // 単価を入力するフィールド
    private JButton processButton; // 処理を実行するボタン
    private JTextArea outputArea; // 処理結果を表示するエリア

    public StoreApp() {
        // Receiptオブジェクトに商品情報を追加
        Receipt receipt = new Receipt();
        // ProductItemを初期化10個
        ProductItem[] productItem = new ProductItem[10];
        productItem[0] = new ProductItem();
        productItem[0].productName = "りんご";
        productItem[0].unitPrice = 100;
        productItem[0].quantity = 1;

        productItem[1] = new ProductItem();
        productItem[1].productName = "みかん";
        productItem[1].unitPrice = 200;
        productItem[1].quantity = 1;

        productItem[2] = new ProductItem();
        productItem[2].productName = "バナナ";
        productItem[2].unitPrice = 300;
        productItem[2].quantity = 1;

        productItem[3] = new ProductItem();
        productItem[3].productName = "ぶどう";
        productItem[3].unitPrice = 400;
        productItem[3].quantity = 1;

        productItem[4] = new ProductItem();
        productItem[4].productName = "もも";
        productItem[4].unitPrice = 500;
        productItem[4].quantity = 1;

        productItem[5] = new ProductItem();
        productItem[5].productName = "さくらんぼ";
        productItem[5].unitPrice = 600;
        productItem[5].quantity = 1;

        productItem[6] = new ProductItem();
        productItem[6].productName = "いちご";
        productItem[6].unitPrice = 700;
        productItem[6].quantity = 1;

        productItem[7] = new ProductItem();
        productItem[7].productName = "パイナップル";
        productItem[7].unitPrice = 800;
        productItem[7].quantity = 1;

        productItem[8] = new ProductItem();
        productItem[8].productName = "キウイ";
        productItem[8].unitPrice = 900;
        productItem[8].quantity = 1;

        productItem[9] = new ProductItem();
        productItem[9].productName = "グレープフルーツ";
        productItem[9].unitPrice = 1000;

        // --- ウィンドウの基本設定 ---
        setTitle("レジ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);

        // --- レイアウトにBorderLayoutを採用 ---
        // 部品間の隙間を縦横5ピクセルに設定
        setLayout(new BorderLayout(5, 5));

        // GridBagLayoutを使用して柔軟な配置を行う
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // GridBagConstraintsのデフォルト設定
        gbc.insets = new Insets(5, 5, 5, 5); // 部品間の余白
        gbc.anchor = GridBagConstraints.WEST; // 左寄せを基本とする

        // --- 1行目: 商品名ラベルとフィールド ---
        // 商品名ラベル (gridx=0, gridy=0)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0; // ラベル列は伸縮させない
        gbc.fill = GridBagConstraints.NONE; // サイズ変更しない
        gbc.anchor = GridBagConstraints.EAST; // ラベルを右寄せにする
        JLabel productNameLabel = new JLabel("JANコード:");
        topPanel.add(productNameLabel, gbc);

        // 商品名フィールド (gridx=1, gridy=0)
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // フィールド列は横方向に伸縮させる
        gbc.fill = GridBagConstraints.HORIZONTAL; // 横方向にいっぱいに広げる
        productJanCodeField = new JTextField();
        topPanel.add(productJanCodeField, gbc);

        // --- 4行目: 計算ボタン ---
        // ボタン (gridx=1, gridy=3) 右寄せで配置
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 0.0; // ボタン自体は伸縮させない
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST; // ボタンを右端に寄せる
        processButton = new JButton("追加");
        topPanel.add(processButton, gbc);

        // --- 中央に配置する部品 (結果表示エリア) ---
        outputArea = new JTextArea();
        // outputArea.setEditable(false); // 必要に応じて編集不可に設定
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // --- 部品をウィンドウに追加 ---
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // --- 商品追加ボタンのアクション設定 ---
        processButton.addActionListener(e -> {
            // 各フィールドからテキストを取得
            String productJanCode = productJanCodeField.getText();

            // 入力が空でないか基本的なチェック
            if (productJanCode.isEmpty()) {
                System.err.println("未入力項目があります。");
                return; // 処理を中断
            }

            // 単価と数量を数値に変換
            // 単価は小数点を含む可能性があるためdoubleを使用
            double unitPrice = Double.parseDouble(productJanCode); // 商品名を単価として扱う

            // 商品情報をProductItemオブジェクトに格納
            ProductItem item = new ProductItem();
            item.productName = productJanCode; // 商品名
            item.unitPrice = unitPrice; // 単価
            item.quantity = 1; // 数量
            receipt.addProduct(item); // 商品情報をレシートに追加

            // 結果をフォーマットしてテキストエリアに追加
            // ProductItemのtoStringメソッドを使用して整形済み文字列を取得
            String outputLine = item.toString(); // 商品情報を整形済み文字列に変換
            // appendメソッドで追記
            outputArea.append(outputLine);

            // 入力フィールドをクリアする
            productJanCodeField.setText("");

            // 商品名の入力フィールドにフォーカスを移動
            productJanCodeField.requestFocus();

        });

        // --- ウィンドウを表示 ---
        setVisible(true);
    }

    public static void main(String[] args) {
        // イベントディスパッチスレッドでGUIを作成・実行
        SwingUtilities.invokeLater(() -> new StoreApp());
    }
}