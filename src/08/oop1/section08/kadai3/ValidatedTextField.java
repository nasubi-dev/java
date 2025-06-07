package oop1.section08.kadai3;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ValidatedTextField extends JTextField {
  private final InputValidator validator;

  public ValidatedTextField(InputValidator validator, int columns) {
    super(columns);
    this.validator = validator;

    // フォーカスリスナーを追加
    this.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        String currentText = getText();

        try {
          validator.validate(currentText);
        } catch (ValidationException ex) {
          JOptionPane.showMessageDialog(ValidatedTextField.this, ex.getMessage(), "入力エラー", JOptionPane.ERROR_MESSAGE);

          SwingUtilities.invokeLater(() -> {
            ValidatedTextField.this.requestFocusInWindow();
            ValidatedTextField.this.selectAll();
          });
        }
      }
    });
  }
}
