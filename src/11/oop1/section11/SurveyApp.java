package oop1.section11;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SurveyApp extends JFrame {
  private static final String CSV_FILE_PATH = "./src/11/oop1/section11/survey_results.csv";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private JTextField nameField;
  private ButtonGroup ageGroup;
  private JRadioButton age20s, age30s, age40s;
  private JCheckBox programmingCheck, designCheck, travelCheck;
  private JButton submitButton;
  private JTextArea resultArea;

  public SurveyApp() {
    setTitle("アンケート集計アプリ");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    initializeComponents();
    setupLayout();
    setupEventHandlers();
    loadExistingData();
    setSize(600, 500);
    setLocationRelativeTo(null);
  }

  private void initializeComponents() {
    nameField = new JTextField(20);

    age20s = new JRadioButton("20代");
    age30s = new JRadioButton("30代");
    age40s = new JRadioButton("40代");
    ageGroup = new ButtonGroup();
    ageGroup.add(age20s);
    ageGroup.add(age30s);
    ageGroup.add(age40s);

    programmingCheck = new JCheckBox("プログラミング");
    designCheck = new JCheckBox("デザイン");
    travelCheck = new JCheckBox("旅行");

    submitButton = new JButton("回答を送信");

    resultArea = new JTextArea(15, 50);
    resultArea.setEditable(false);
    resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
  }

  private void setupLayout() {
    setLayout(new BorderLayout());

    // 入力フォーム部分
    JPanel formPanel = new JPanel();
    formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
    formPanel.setBorder(BorderFactory.createTitledBorder("アンケート入力"));

    // 氏名入力部分
    JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    namePanel.add(new JLabel("氏名:"));
    namePanel.add(nameField);
    formPanel.add(namePanel);

    // 年代選択部分
    JPanel agePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    agePanel.add(new JLabel("年代:"));
    agePanel.add(age20s);
    agePanel.add(age30s);
    agePanel.add(age40s);
    formPanel.add(agePanel);

    // 興味分野選択部分
    JPanel interestPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    interestPanel.add(new JLabel("興味のある分野:"));
    interestPanel.add(programmingCheck);
    interestPanel.add(designCheck);
    interestPanel.add(travelCheck);
    formPanel.add(interestPanel);

    // 送信ボタン部分
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add(submitButton);
    formPanel.add(buttonPanel);

    // 結果表示部分
    JPanel resultPanel = new JPanel(new BorderLayout());
    resultPanel.setBorder(BorderFactory.createTitledBorder("回答履歴"));
    JScrollPane scrollPane = new JScrollPane(resultArea);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    resultPanel.add(scrollPane, BorderLayout.CENTER);

    add(formPanel, BorderLayout.NORTH);
    add(resultPanel, BorderLayout.CENTER);
  }

  private void setupEventHandlers() {
    submitButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        submitSurvey();
      }
    });

    nameField.addActionListener(e -> submitSurvey());
  }

  private void submitSurvey() {
    try {
      String name = nameField.getText().trim();
      if (name.isEmpty()) {
        showErrorDialog("氏名を入力してください。");
        return;
      }

      String age = getSelectedAge();
      if (age == null) {
        showErrorDialog("年代を選択してください。");
        return;
      }

      List<String> interests = getSelectedInterests();

      String timestamp = LocalDateTime.now().format(DATE_FORMATTER);

      String interestString = String.join(";", interests);
      String csvLine = String.format("%s,%s,%s,%s", timestamp, name, age, interestString);

      saveToFile(csvLine);

      String displayLine = String.format("[%s] %s (%s) - %s\n",
          timestamp, name, age,
          interests.isEmpty() ? "なし" : String.join(", ", interests));
      resultArea.append(displayLine);
      resultArea.setCaretPosition(resultArea.getDocument().getLength());

      clearForm();

      JOptionPane.showMessageDialog(this, "回答を送信しました。", "送信完了", JOptionPane.INFORMATION_MESSAGE);

    } catch (Exception e) {
      showErrorDialog("回答の送信中にエラーが発生しました: " + e.getMessage());
    }
  }

  private String getSelectedAge() {
    if (age20s.isSelected())
      return "20代";
    if (age30s.isSelected())
      return "30代";
    if (age40s.isSelected())
      return "40代";
    return null;
  }

  private List<String> getSelectedInterests() {
    List<String> interests = new ArrayList<>();
    if (programmingCheck.isSelected())
      interests.add("プログラミング");
    if (designCheck.isSelected())
      interests.add("デザイン");
    if (travelCheck.isSelected())
      interests.add("旅行");
    return interests;
  }

  private void saveToFile(String csvLine) throws IOException {
    try (FileWriter writer = new FileWriter(CSV_FILE_PATH, true);
        BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
      bufferedWriter.write(csvLine);
      bufferedWriter.newLine();
    }
  }

  private void loadExistingData() {
    File csvFile = new File(CSV_FILE_PATH);
    if (!csvFile.exists()) {
      resultArea.setText("過去の回答はありません。\n");
      return;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
      StringBuilder content = new StringBuilder();
      String line;

      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",", 4);
        if (parts.length >= 4) {
          String timestamp = parts[0];
          String name = parts[1];
          String age = parts[2];
          String interests = parts[3];

          // 興味分野の表示形式を整える
          String interestDisplay = interests.isEmpty() ? "なし" : interests.replace(";", ", ");

          content.append(String.format("[%s] %s (%s) - %s\n",
              timestamp, name, age, interestDisplay));
        }
      }

      if (content.length() == 0) {
        resultArea.setText("過去の回答はありません。\n");
      } else {
        resultArea.setText(content.toString());
      }

    } catch (IOException e) {
      showErrorDialog("既存のデータの読み込み中にエラーが発生しました: " + e.getMessage());
      resultArea.setText("データの読み込みに失敗しました。\n");
    }
  }

  private void clearForm() {
    nameField.setText("");
    ageGroup.clearSelection();
    programmingCheck.setSelected(false);
    designCheck.setSelected(false);
    travelCheck.setSelected(false);
  }

  private void showErrorDialog(String message) {
    JOptionPane.showMessageDialog(this, message, "エラー", JOptionPane.ERROR_MESSAGE);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      new SurveyApp().setVisible(true);
    });
  }
}
