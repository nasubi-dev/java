package oop1.kadai06;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.util.Map;

public class PartTimeEmployeeFactory implements EmployeeFactory {

  @Override
  public Employee createEmployee(JFrame parentFrame, Map<String, JComponent> inputFields) {
    try {
      // 必須入力フィールドの取得と検証
      JTextField idField = (JTextField) inputFields.get("employeeId");
      JTextField nameField = (JTextField) inputFields.get("name");
      JTextField basePayField = (JTextField) inputFields.get("basePay");
      JTextField hoursWorkedField = (JTextField) inputFields.get("hoursWorked");

      String employeeId = idField.getText().trim();
      String name = nameField.getText().trim();
      String basePayText = basePayField.getText().trim();
      String hoursWorkedText = hoursWorkedField.getText().trim();

      // 必須フィールドの空チェック
      if (employeeId.isEmpty() || name.isEmpty() || basePayText.isEmpty() ||
          hoursWorkedText.isEmpty()) {

        JOptionPane.showMessageDialog(parentFrame,
            "従業員ID、氏名、時給、労働時間は必須入力です。",
            "入力エラー", JOptionPane.ERROR_MESSAGE);
        return null;
      }

      // 数値変換
      double hourlyRate = Double.parseDouble(basePayText);
      double hoursWorked = Double.parseDouble(hoursWorkedText);

      // PartTimeEmployeeオブジェクトの生成と返却
      return new PartTimeEmployee(employeeId, name, hourlyRate, hoursWorked);

    } catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(parentFrame,
          "数値の入力形式が正しくありません。",
          "入力エラー", JOptionPane.ERROR_MESSAGE);
      return null;
    }
  }

  @Override
  public void updateFieldVisibility(Map<String, Boolean> visibilityMap) {
    // アルバイト用のフィールドは表示、正社員用のフィールドは非表示
    visibilityMap.put("overtimeHours", false);
    visibilityMap.put("bonus", false);
    visibilityMap.put("commuteAllowance", false);
    visibilityMap.put("hoursWorked", true);
  }
}
