package oop1.kadai06;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.util.Map;

public class FullTimeEmployeeFactory implements EmployeeFactory {

  @Override
  public Employee createEmployee(JFrame parentFrame, Map<String, JComponent> inputFields) {
    try {
      // 必須入力フィールドの取得と検証
      JTextField idField = (JTextField) inputFields.get("employeeId");
      JTextField nameField = (JTextField) inputFields.get("name");
      JTextField basePayField = (JTextField) inputFields.get("basePay");
      JTextField overtimeHoursField = (JTextField) inputFields.get("overtimeHours");
      JTextField bonusField = (JTextField) inputFields.get("bonus");
      JTextField commuteAllowanceField = (JTextField) inputFields.get("commuteAllowance");

      String employeeId = idField.getText().trim();
      String name = nameField.getText().trim();
      String basePayText = basePayField.getText().trim();
      String overtimeHoursText = overtimeHoursField.getText().trim();
      String bonusText = bonusField.getText().trim();
      String commuteAllowanceText = commuteAllowanceField.getText().trim();

      // 必須フィールドの空チェック
      if (employeeId.isEmpty() || name.isEmpty() || basePayText.isEmpty() ||
          overtimeHoursText.isEmpty() || bonusText.isEmpty() || commuteAllowanceText.isEmpty()) {

        JOptionPane.showMessageDialog(parentFrame,
            "従業員ID、氏名、基本給、残業時間、賞与、交通費は必須入力です。",
            "入力エラー", JOptionPane.ERROR_MESSAGE);
        return null;
      }

      // 数値変換
      double basePay = Double.parseDouble(basePayText);
      double overtimeHours = Double.parseDouble(overtimeHoursText);
      double bonus = Double.parseDouble(bonusText);
      double commuteAllowance = Double.parseDouble(commuteAllowanceText);

      // FullTimeEmployeeオブジェクトの生成と返却
      return new FullTimeEmployee(employeeId, name, basePay, overtimeHours, bonus, commuteAllowance);

    } catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(parentFrame,
          "数値の入力形式が正しくありません。",
          "入力エラー", JOptionPane.ERROR_MESSAGE);
      return null;
    }
  }

  @Override
  public void updateFieldVisibility(Map<String, Boolean> visibilityMap) {
    // 正社員用のフィールドは表示、アルバイト用のフィールドは非表示
    visibilityMap.put("overtimeHours", true);
    visibilityMap.put("bonus", true);
    visibilityMap.put("commuteAllowance", true);
    visibilityMap.put("hoursWorked", false);
  }
}
