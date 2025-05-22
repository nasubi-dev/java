package oop1.kadai06;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

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

  // 従業員タイプと対応するファクトリーのマップ
  private Map<String, EmployeeFactory> employeeFactories;

  public SalarySimulatorFrame() {
    setTitle("簡易給与計算シミュレータ");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(800, 600);
    setLocationRelativeTo(null); // 画面中央に表示

    // ファクトリーマップの初期化
    initEmployeeFactories();

    initComponents();
    layoutComponents();
    attachListeners();

    // 初期状態で正社員用のフィールドを表示
    updateInputFieldsVisibility("正社員");
  }

  private void initEmployeeFactories() {
    employeeFactories = new HashMap<>();
    employeeFactories.put("正社員", new FullTimeEmployeeFactory());
    employeeFactories.put("アルバイト", new PartTimeEmployeeFactory());
    // 将来的に新しい従業員タイプが追加された場合も、ここに追加するだけでOK
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
    // 選択された従業員タイプに対応するファクトリーを取得
    EmployeeFactory factory = employeeFactories.get(selectedType);
    if (factory == null)
      return;

    // 表示状態を更新するマップを作成
    Map<String, Boolean> visibilityMap = new HashMap<>();

    // ファクトリーに表示設定を委譲
    factory.updateFieldVisibility(visibilityMap);

    // マップに基づいて表示状態を更新
    overtimeHoursLabel.setVisible(visibilityMap.get("overtimeHours"));
    overtimeHoursField.setVisible(visibilityMap.get("overtimeHours"));
    bonusLabel.setVisible(visibilityMap.get("bonus"));
    bonusField.setVisible(visibilityMap.get("bonus"));
    commuteAllowanceLabel.setVisible(visibilityMap.get("commuteAllowance"));
    commuteAllowanceField.setVisible(visibilityMap.get("commuteAllowance"));
    hoursWorkedLabel.setVisible(visibilityMap.get("hoursWorked"));
    hoursWorkedField.setVisible(visibilityMap.get("hoursWorked"));
  }

  private void attachListeners() {
    // layoutComponents内で設定済み
  }

  private class CalculateButtonListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      // 選択された従業員タイプの取得
      String employeeType = (String) employeeTypeComboBox.getSelectedItem();

      // 選択された従業員タイプに対応するファクトリーを取得
      EmployeeFactory factory = employeeFactories.get(employeeType);
      if (factory == null) {
        JOptionPane.showMessageDialog(SalarySimulatorFrame.this,
            "選択された従業員タイプは対応していません。",
            "エラー", JOptionPane.ERROR_MESSAGE);
        return;
      }

      // 入力フィールドのマップを作成
      Map<String, JComponent> inputFields = new HashMap<>();
      inputFields.put("employeeId", employeeIdField);
      inputFields.put("name", nameField);
      inputFields.put("basePay", basePayField);
      inputFields.put("overtimeHours", overtimeHoursField);
      inputFields.put("bonus", bonusField);
      inputFields.put("commuteAllowance", commuteAllowanceField);
      inputFields.put("hoursWorked", hoursWorkedField);

      // ファクトリーに従業員オブジェクトの生成を委譲
      Employee employee = factory.createEmployee(SalarySimulatorFrame.this, inputFields);

      // 従業員オブジェクトが正常に生成できた場合のみ給与明細を表示
      if (employee != null) {
        paySlipPanel.displayPaySlip(employee);
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
