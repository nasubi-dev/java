package oop1.todoapp;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

class TaskCellRenderer extends DefaultListCellRenderer {

  @Override
  public Component getListCellRendererComponent(
      JList<?> list,
      Object value,
      int index,
      boolean isSelected,
      boolean cellHasFocus) {

    // 親クラスの実装を呼び出し、基本的なJLabelコンポーネントを取得
    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

    // valueをTask型にキャストして利用
    if (value instanceof Task) {
      Task task = (Task) value;

      // TaskオブジェクトのtoString()メソッドを利用して表示テキストを設定
      label.setText(task.toString());

      // Taskオブジェクトの状態に基づき、ラベルの視覚的プロパティを変更
      if (task.isCompleted()) {
        // 完了したタスクはグレー色で表示
        if (!isSelected) {
          label.setForeground(Color.GRAY);
        }

        // フォント属性を操作して取り消し線を追加
        Map<TextAttribute, Object> attributes = new HashMap<>(label.getFont().getAttributes());
        attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        label.setFont(label.getFont().deriveFont(attributes));
      } else {
        // 未完了タスクはデフォルトのスタイル
        if (!isSelected) {
          label.setForeground(list.getForeground());
        }

        Map<TextAttribute, Object> attributes = new HashMap<>(label.getFont().getAttributes());
        attributes.put(TextAttribute.STRIKETHROUGH, false);
        label.setFont(label.getFont().deriveFont(attributes));
      }
    }

    return label;
  }
}
