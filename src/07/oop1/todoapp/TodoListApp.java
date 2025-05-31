package oop1.todoapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class TodoListApp {

  private JFrame frame; // メインウィンドウ
  private DefaultListModel<Task> listModel; // JListのモデル (Taskオブジェクトを格納)
  private JList<Task> taskList; // タスク表示用リスト (Taskオブジェクトを表示)
  private JTextField taskInput; // タスク内容入力用テキストフィールド
  private JTextField dueDateInput; // 期限日入力用テキストフィールド
  private List<Task> tasks; // タスクを格納するArrayList (Taskオブジェクトのリスト)

  public TodoListApp() {
    // データ構造の初期化
    tasks = new ArrayList<>();
    listModel = new DefaultListModel<>();

    // メインフレームの設定
    frame = new JFrame("TODO");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(750, 450);
    frame.setLayout(new BorderLayout(5, 5)); // コンポーネント間の隙間を設定

    // 入力パネルの作成
    JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5)); // 左揃え、コンポーネント間隔5px

    // タスク内容入力フィールド
    inputPanel.add(new JLabel("タスク内容:"));
    taskInput = new JTextField(20); // 幅の目安として20文字分
    inputPanel.add(taskInput);

    // 期限日入力フィールド
    inputPanel.add(new JLabel("期限日 (YYYY-MM-DD):"));
    dueDateInput = new JTextField(10); // 幅の目安として10文字分 (YYYY-MM-DD)
    inputPanel.add(dueDateInput);

    // 追加ボタン
    JButton addButton = new JButton("追加");
    addButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        addTask();
      }
    });
    inputPanel.add(addButton);

    frame.add(inputPanel, BorderLayout.NORTH); // フレームの上部に入力パネルを追加

    // タスク表示リストの作成
    taskList = new JList<>(listModel); // listModelを使用してJListを初期化
    taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 単一選択モードに設定

    // カスタムセルレンダラーを設定
    taskList.setCellRenderer(new TaskCellRenderer());

    JScrollPane scrollPane = new JScrollPane(taskList); // リストをスクロール可能にする
    frame.add(scrollPane, BorderLayout.CENTER); // フレームの中央にリストを追加

    // 操作ボタンパネルの作成
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5)); // 中央揃え、コンポーネント間隔(左右10px, 上下5px)

    // 完了/未完了切り替えボタン
    JButton toggleCompleteButton = new JButton("完了/未完了");
    toggleCompleteButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        toggleTaskCompletion();
      }
    });
    buttonPanel.add(toggleCompleteButton);

    // 削除ボタン
    JButton deleteButton = new JButton("削除");
    deleteButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        deleteTask();
      }
    });
    buttonPanel.add(deleteButton);

    // 期限日ソートボタン
    JButton sortByDueDateButton = new JButton("期限日でソート");
    sortByDueDateButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        sortTasksByDueDate();
      }
    });
    buttonPanel.add(sortByDueDateButton);

    frame.add(buttonPanel, BorderLayout.SOUTH); // フレームの下部にボタンパネルを追加

    // フレームを画面中央に表示し、可視化
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  private void addTask() {
    String description = taskInput.getText().trim();
    String dueDateText = dueDateInput.getText().trim();

    // タスク内容が空の場合は追加しない
    if (description.isEmpty()) {
      JOptionPane.showMessageDialog(frame, "タスク内容を入力してください。", "入力エラー", JOptionPane.WARNING_MESSAGE);
      return;
    }

    // 期限日を解析
    LocalDate dueDate = Taskable.parseDueDate(dueDateText);

    // 新しいタスクを作成して追加
    Task newTask = new Task(description, dueDate);
    tasks.add(newTask);

    // リスト表示を更新
    updateListDisplay();

    // 入力フィールドをクリア
    taskInput.setText("");
    dueDateInput.setText("");

    // タスク内容入力フィールドにフォーカスを戻す
    taskInput.requestFocus();
  }

  private void toggleTaskCompletion() {
    int selectedIndex = taskList.getSelectedIndex();
    if (selectedIndex == -1) {
      JOptionPane.showMessageDialog(frame, "切り替えるタスクを選択してください。", "選択エラー", JOptionPane.WARNING_MESSAGE);
      return;
    }

    Task selectedTask = tasks.get(selectedIndex);
    selectedTask.setCompleted(!selectedTask.isCompleted());

    // リスト表示を更新
    updateListDisplay();

    // 選択状態を維持
    taskList.setSelectedIndex(selectedIndex);
  }

  private void deleteTask() {
    int selectedIndex = taskList.getSelectedIndex();
    if (selectedIndex == -1) {
      JOptionPane.showMessageDialog(frame, "削除するタスクを選択してください。", "選択エラー", JOptionPane.WARNING_MESSAGE);
      return;
    }

    // 確認ダイアログを表示
    int result = JOptionPane.showConfirmDialog(frame, "選択されたタスクを削除しますか？", "削除確認", JOptionPane.YES_NO_OPTION);
    if (result == JOptionPane.YES_OPTION) {
      tasks.remove(selectedIndex);
      updateListDisplay();
    }
  }

  private void sortTasksByDueDate() {
    tasks.sort(new Comparator<Task>() {
      @Override
      public int compare(Task t1, Task t2) {
        LocalDate date1 = t1.getDueDate();
        LocalDate date2 = t2.getDueDate();

        // 両方ともnullの場合は等しい
        if (date1 == null && date2 == null) {
          return 0;
        }
        // date1がnullの場合、date2より後（大きい）
        if (date1 == null) {
          return 1;
        }
        // date2がnullの場合、date1より前（小さい）
        if (date2 == null) {
          return -1;
        }
        // 両方ともnullでない場合は、日付で比較
        return date1.compareTo(date2);
      }
    });

    // リスト表示を更新
    updateListDisplay();
  }

  private void updateListDisplay() {
    listModel.clear();
    for (Task task : tasks) {
      listModel.addElement(task);
    }
    taskList.repaint();
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new TodoListApp());
  }
}
