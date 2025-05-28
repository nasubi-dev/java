package oop1.kadai06;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class SalarySimulatorFrame extends JFrame {
  private JComboBox<String> employeeTypeComboBox;
  private JTextField employeeIdField;
  private JTextField nameField;
  private JTextField basePayField;
  private JTextField overtimeHoursField;
  private JTextField bonusField;
  private JTextField commuteAllowanceField;
  private JTextField hoursWorkedField;
  private JLabel overtimeHoursLabel;
  private JLabel bonusLabel;
  private JLabel commuteAllowanceLabel;
  private JLabel hoursWorkedLabel;
  private PaySlipPanel paySlipPanel;

  public SalarySimulatorFrame() {
    setTitle("簡易給与計算シミュレータ");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(800, 600);
    setLocationRelativeTo(null); // 画面中央に表示

    initComponents();
    layoutComponents();
    attachListeners();

    // 初期状態でアルバイト用のフィールドを非表示にする
    updateInputFieldsVisibility("正社員"); // 初期選択は正社員に合わせる
  }

  private void initComponents() {
    employeeTypeComboBox = new JComboBox<>(new String[] { "正社員", "アルバイト" });
    employeeIdField = new JTextField(15);
    nameField = new JTextField(15);
    basePayField = new JTextField(10);
    overtimeHoursField = new JTextField(5);
    bonusField = new JTextField(10);
    commuteAllowanceField = new JTextField(10);
    hoursWorkedField = new JTextField(5);

    overtimeHoursLabel = new JLabel("残業時間 (h):");
    bonusLabel = new JLabel("賞与 (円):");
    commuteAllowanceLabel = new JLabel("交通費 (円):");
    hoursWorkedLabel = new JLabel("労働時間 (h):");

    paySlipPanel = new PaySlipPanel();
  }

  private void layoutComponents() {
    // 入力パネルの作成
    JPanel inputPanel = new JPanel(new GridBagLayout());
    inputPanel.setBorder(BorderFactory.createTitledBorder("従業員情報入力"));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5); // コンポーネント間の余白
    gbc.anchor = GridBagConstraints.WEST; // 左寄せ

    // 1行目: 従業員種別
    gbc.gridx = 0;
    gbc.gridy = 0;
    inputPanel.add(new JLabel("従業員種別:"), gbc);
    gbc.gridx = 1;
    inputPanel.add(employeeTypeComboBox, gbc);

    // 2行目: 従業員ID
    gbc.gridx = 0;
    gbc.gridy = 1;
    inputPanel.add(new JLabel("従業員ID:"), gbc);
    gbc.gridx = 1;
    inputPanel.add(employeeIdField, gbc);

    // 3行目: 氏名
    gbc.gridx = 0;
    gbc.gridy = 2;
    inputPanel.add(new JLabel("氏名:"), gbc);
    gbc.gridx = 1;
    inputPanel.add(nameField, gbc);

    // 4行目: 基本給/時給
    gbc.gridx = 0;
    gbc.gridy = 3;
    inputPanel.add(new JLabel("基本給/時給 (円):"), gbc);
    gbc.gridx = 1;
    inputPanel.add(basePayField, gbc);

    // 5行目: 残業時間 (正社員用)
    gbc.gridx = 0;
    gbc.gridy = 4;
    inputPanel.add(overtimeHoursLabel, gbc);
    gbc.gridx = 1;
    inputPanel.add(overtimeHoursField, gbc);

    // 6行目: 賞与 (正社員用)
    gbc.gridx = 0;
    gbc.gridy = 5;
    inputPanel.add(bonusLabel, gbc);
    gbc.gridx = 1;
    inputPanel.add(bonusField, gbc);

    // 7行目: 交通費 (正社員用)
    gbc.gridx = 0;
    gbc.gridy = 6;
    inputPanel.add(commuteAllowanceLabel, gbc);
    gbc.gridx = 1;
    inputPanel.add(commuteAllowanceField, gbc);

    // 8行目: 労働時間 (アルバイト用)
    gbc.gridx = 0;
    gbc.gridy = 7;
    inputPanel.add(hoursWorkedLabel, gbc);
    gbc.gridx = 1;
    inputPanel.add(hoursWorkedField, gbc);

    // ボタンパネルの作成
    JButton calculateButton = new JButton("給与計算実行");
    JButton clearButton = new JButton("入力クリア");
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add(calculateButton);
    buttonPanel.add(clearButton);

    // 入力フォームとボタンをまとめるパネル
    JPanel formPanel = new JPanel(new BorderLayout());
    formPanel.add(inputPanel, BorderLayout.CENTER);
    formPanel.add(buttonPanel, BorderLayout.SOUTH);

    // メインフレームのレイアウト
    setLayout(new BorderLayout(10, 10)); // パネル間の隙間
    add(formPanel, BorderLayout.WEST);
    add(paySlipPanel, BorderLayout.CENTER);

    // イベントリスナーの設定
    calculateButton.addActionListener(new CalculateButtonListener());
    clearButton.addActionListener(new ClearButtonListener());
    employeeTypeComboBox.addItemListener(new EmployeeTypeChangeListener());
  }

  private void updateInputFieldsVisibility(String selectedType) {
    boolean isFullTime = "正社員".equals(selectedType);

    overtimeHoursLabel.setVisible(isFullTime);
    overtimeHoursField.setVisible(isFullTime);
    bonusLabel.setVisible(isFullTime);
    bonusField.setVisible(isFullTime);
    commuteAllowanceLabel.setVisible(isFullTime);
    commuteAllowanceField.setVisible(isFullTime);

    hoursWorkedLabel.setVisible(!isFullTime);
    hoursWorkedField.setVisible(!isFullTime);
  }

  private void attachListeners() {
    // layoutComponents内で設定済み
  }

  private class CalculateButtonListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        String employeeType = (String) employeeTypeComboBox.getSelectedItem();
        String employeeId = employeeIdField.getText().trim();
        String name = nameField.getText().trim();

        // 必須入力フィールドの検証
        if (employeeId.isEmpty() || name.isEmpty() || basePayField.getText().trim().isEmpty()) {
          JOptionPane.showMessageDialog(SalarySimulatorFrame.this,
              "従業員ID、氏名、基本給/時給は必須入力です。",
              "入力エラー", JOptionPane.ERROR_MESSAGE);
          return;
        }

        // 基本給/時給の数値変換
        double basePay = Double.parseDouble(basePayField.getText().trim());

        Employee employee = null;

        if ("正社員".equals(employeeType)) {
          // 残業時間、賞与、交通費の検証と数値変換
          if (overtimeHoursField.getText().trim().isEmpty() ||
              bonusField.getText().trim().isEmpty() ||
              commuteAllowanceField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(SalarySimulatorFrame.this,
                "残業時間、賞与、交通費は必須入力です。",
                "入力エラー", JOptionPane.ERROR_MESSAGE);
            return;
          }

          double overtimeHours = Double.parseDouble(overtimeHoursField.getText().trim());
          double bonus = Double.parseDouble(bonusField.getText().trim());
          double commuteAllowance = Double.parseDouble(commuteAllowanceField.getText().trim());

          // FullTimeEmployeeオブジェクトの生成
          employee = new FullTimeEmployee(employeeId, name, basePay, overtimeHours, bonus, commuteAllowance);

        } else if ("アルバイト".equals(employeeType)) {
          // 労働時間の検証と数値変換
          if (hoursWorkedField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(SalarySimulatorFrame.this,
                "労働時間は必須入力です。",
                "入力エラー", JOptionPane.ERROR_MESSAGE);
            return;
          }

          double hoursWorked = Double.parseDouble(hoursWorkedField.getText().trim());

          // PartTimeEmployeeオブジェクトの生成
          employee = new PartTimeEmployee(employeeId, name, basePay, hoursWorked);
        }

        // PaySlipPanelに従業員オブジェクトを渡して給与明細を表示
        paySlipPanel.displayPaySlip(employee);

      } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(SalarySimulatorFrame.this,
            "数値の入力形式が正しくありません。",
            "入力エラー", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private class ClearButtonListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      // すべての入力フィールドをクリア
      employeeIdField.setText("");
      nameField.setText("");
      basePayField.setText("");
      overtimeHoursField.setText("");
      bonusField.setText("");
      commuteAllowanceField.setText("");
      hoursWorkedField.setText("");

      // コンボボックスを最初の選択肢（正社員）に戻す
      employeeTypeComboBox.setSelectedIndex(0);

      // 給与明細パネルをクリア
      paySlipPanel.clearPaySlip();
    }
  }

  private class EmployeeTypeChangeListener implements ItemListener {
    @Override
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        String selectedType = (String) e.getItem();
        updateInputFieldsVisibility(selectedType);
      }
    }
  }
}
