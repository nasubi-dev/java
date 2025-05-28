import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SelectableGuiApp {
  private JFrame frame;
  private ExecutableRadioButton taskARadio;
  private ExecutableRadioButton taskBRadio;
  private ButtonGroup buttonGroup;
  private JButton executeButton;
  private JLabel resultLabel;

  private void initializeGUI() {
    // JFrameを作成
    frame = new JFrame("処理選択デモ");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(400, 200);
    frame.setLocationRelativeTo(null); // 画面中央に配置

    // JPanelを作成
    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(4, 1, 10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // ラジオボタンを作成
    taskARadio = new ExecutableRadioButton("タスクAを実行", new TaskA(), true); // デフォルトで選択
    taskBRadio = new ExecutableRadioButton("タスクBを実行", new TaskB());

    // ButtonGroupでラジオボタンをグループ化
    buttonGroup = new ButtonGroup();
    buttonGroup.add(taskARadio);
    buttonGroup.add(taskBRadio);

    // 実行ボタンを作成
    executeButton = new JButton("実行");

    // 結果表示用のラベルを作成
    resultLabel = new JLabel("結果: （実行ボタンを押してください）");
    resultLabel.setHorizontalAlignment(SwingConstants.CENTER);

    // ActionListenerを追加
    executeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        executeSelectedTask();
      }
    });

    // コンポーネントをパネルに追加
    panel.add(taskARadio);
    panel.add(taskBRadio);
    panel.add(executeButton);
    panel.add(resultLabel);

    // パネルをフレームに追加
    frame.add(panel);
  }

  private void executeSelectedTask() {
    // ButtonGroupから選択されたExecutableRadioButtonを取得
    ExecutableRadioButton selectedRadio = getSelectedExecutableRadioButton();

    if (selectedRadio == null) {
      resultLabel.setText("結果: エラー - タスクが選択されていません");
      return;
    }

    // if文なしで直接Executableを取得し実行
    Executable selectedTask = selectedRadio.getExecutable();
    String result = selectedTask.execute();
    resultLabel.setText("結果: " + result);
  }

  private ExecutableRadioButton getSelectedExecutableRadioButton() {
    // ButtonGroupのすべてのボタンを取得してチェック
    for (AbstractButton button : java.util.Collections.list(buttonGroup.getElements())) {
      if (button.isSelected() && button instanceof ExecutableRadioButton) {
        return (ExecutableRadioButton) button;
      }
    }
    return null;
  }

  public void showGUI() {
    SwingUtilities.invokeLater(() -> {
      initializeGUI();
      frame.setVisible(true);
    });
  }

  public static void main(String[] args) {
    SelectableGuiApp app = new SelectableGuiApp();
    app.showGUI();
  }
}
