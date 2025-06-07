package oop1.section08.kadai3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * フォーカス喪失時に入力値を処理するカスタムJTextField
 */
class MyValidatedTextField extends JTextField {

    public MyValidatedTextField(int columns) {
        super(columns); // 親クラスJTextFieldのコンストラクタを呼び出す

        // FocusListenerを登録する
        // FocusAdapter を使用すると、必要なメソッドだけをオーバーライドできる
        this.addFocusListener(new FocusAdapter() {
            /**
             * テキストフィールドからフォーカスが失われたときに呼び出されるメソッド
             * @param e フォーカスイベントに関する情報
             */
            @Override
            public void focusLost(FocusEvent e) {
                // 現在のテキストフィールドのテキストを取得
                String currentText = getText();

                System.out.println("フォーカスが失われました。入力内容: 「" + currentText + "」");

                // 簡単な検証ロジックの例:
                // 入力が空文字列かどうかをチェック
                if (currentText.trim().isEmpty()) {
                    // 空の場合は背景色を赤に変更
                    setBackground(Color.PINK);
                    System.out.println("入力が空です。背景色を変更しました。");
                } else {
                    // 空でなければ背景色をデフォルト（白）に戻す
                    setBackground(Color.WHITE);
                    System.out.println("入力は空ではありません。");
                }
                // ここで、より複雑な入力検証ロジックを呼び出すことができる
                // (例: validator.validate(currentText); のような形)
            }

            /**
             * テキストフィールドがフォーカスを得たときに呼び出されるメソッド (この例では特に処理なし)
             * @param e フォーカスイベントに関する情報
             */
            @Override
            public void focusGained(FocusEvent e) {
                // フォーカスを得たときに背景色をデフォルトに戻すなどの処理も可能
                setBackground(Color.WHITE);

                System.out.println("フォーカス取得。");
            }
        });
    }
}

/**
 * MyValidatedTextFieldの動作を確認するための簡単なデモクラス
 */
public class FocusLostDemo {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Focus Lost Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new FlowLayout());

            MyValidatedTextField validatedField = new MyValidatedTextField(15);
            JTextField textField = new JTextField("他のフィールド", 15); // フォーカス遷移先である別のフィールド

            frame.add(new JLabel("検証フィールド:"));
            frame.add(validatedField);
            frame.add(new JLabel("別フィールド:"));
            frame.add(textField);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}