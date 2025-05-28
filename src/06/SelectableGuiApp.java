import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SelectableGuiApp {
  private JFrame frame;
  private JRadioButton taskARadio;
  private JRadioButton taskBRadio;
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
    taskARadio = new JRadioButton("タスクAを実行", true); // デフォルトで選択
    taskBRadio = new JRadioButton("タスクBを実行");

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

  /**
   * 選択されたタスクを実行する
   */
  private void executeSelectedTask() {
    Executable selectedTask;

    // ラジオボタンの選択状態に応じてタスクを選択
    if (taskARadio.isSelected()) {
      selectedTask = new TaskA();
    } else if (taskBRadio.isSelected()) {
      selectedTask = new TaskB();
    } else {
      resultLabel.setText("結果: エラー - タスクが選択されていません");
      return;
    }

    // 選択されたタスクを実行し、結果を表示
    String result = selectedTask.execute();
    resultLabel.setText("結果: " + result);
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
