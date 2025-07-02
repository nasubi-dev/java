package oop1.section11;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BmiCalculatorApp extends JFrame {
  private JTextField heightField;
  private JTextField weightField;
  private JLabel resultLabel;
  private JButton calculateButton;

  public BmiCalculatorApp() {
    setTitle("BMI計算機");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    initializeComponents();
    setupLayout();
    setupEventHandlers();
    pack();
    setLocationRelativeTo(null);
  }

  private void initializeComponents() {
    heightField = new JTextField(10);
    weightField = new JTextField(10);
    calculateButton = new JButton("計算実行");
    resultLabel = new JLabel("身長と体重を入力してください");
    resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
    resultLabel.setFont(new Font("Serif", Font.BOLD, 14));
  }

  private void setupLayout() {
    setLayout(new BorderLayout());

    // 入力部分のパネル
    JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
    inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

    inputPanel.add(new JLabel("身長（cm）:"));
    inputPanel.add(heightField);
    inputPanel.add(new JLabel("体重（kg）:"));
    inputPanel.add(weightField);
    inputPanel.add(new JLabel("")); // 空のラベル
    inputPanel.add(calculateButton);

    // 結果表示部分のパネル
    JPanel resultPanel = new JPanel(new FlowLayout());
    resultPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
    resultPanel.add(resultLabel);

    add(inputPanel, BorderLayout.CENTER);
    add(resultPanel, BorderLayout.SOUTH);
  }

  private void setupEventHandlers() {
    calculateButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        calculateBMI();
      }
    });

    // Enterキーでも計算実行
    heightField.addActionListener(e -> calculateBMI());
    weightField.addActionListener(e -> calculateBMI());
  }

  private void calculateBMI() {
    try {
      // 入力値の取得と検証
      String heightText = heightField.getText().trim();
      String weightText = weightField.getText().trim();

      if (heightText.isEmpty() || weightText.isEmpty()) {
        showErrorDialog("身長と体重を両方入力してください。");
        return;
      }

      double height = Double.parseDouble(heightText);
      double weight = Double.parseDouble(weightText);

      if (height <= 0 || weight <= 0) {
        showErrorDialog("身長と体重は正の数値を入力してください。");
        return;
      }

      if (height > 300 || weight > 1000) {
        showErrorDialog("入力値が範囲外です。適切な値を入力してください。");
        return;
      }

      double heightInMeters = height / 100.0;
      double bmi = weight / (heightInMeters * heightInMeters);

      // 判定
      String category = getBMICategory(bmi);

      // 結果表示
      String result = String.format("BMI: %.2f (%s)", bmi, category);
      resultLabel.setText(result);

    } catch (NumberFormatException e) {
      showErrorDialog("数値を正しく入力してください。");
    }
  }

  private String getBMICategory(double bmi) {
    if (bmi < 18.5) {
      return "低体重（痩せ型）";
    } else if (bmi < 25) {
      return "普通体重";
    } else if (bmi < 30) {
      return "肥満（1度）";
    } else {
      return "肥満（2度以上）";
    }
  }

  private void showErrorDialog(String message) {
    JOptionPane.showMessageDialog(this, message, "入力エラー", JOptionPane.ERROR_MESSAGE);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      new BmiCalculatorApp().setVisible(true);
    });
  }
}
