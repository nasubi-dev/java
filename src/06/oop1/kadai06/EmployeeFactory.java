package oop1.kadai06;

import javax.swing.JComponent;
import javax.swing.JFrame;
import java.util.Map;

public interface EmployeeFactory {
  Employee createEmployee(JFrame parentFrame, Map<String, JComponent> inputFields);

  void updateFieldVisibility(Map<String, Boolean> visibilityMap);
}
