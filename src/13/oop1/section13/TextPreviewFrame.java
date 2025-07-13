package oop1.section13;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class TextPreviewFrame extends JFrame {
  private File textFile;
  private JTextArea textArea;

  public TextPreviewFrame(File file) throws IOException {
    this.textFile = file;
    initializeComponents();
    loadTextContent();
    setupWindow();
  }

  private void initializeComponents() {
    textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    textArea.setTabSize(4);

    textArea.setMargin(new Insets(10, 10, 10, 10));
  }

  private void loadTextContent() throws IOException {
    try {
      // ファイルの内容を読み込み
      String content = readFileContent(textFile);
      textArea.setText(content);
      textArea.setCaretPosition(0); // カーソルを先頭に移動

    } catch (IOException e) {
      throw new IOException("テキストファイルの読み込みに失敗しました: " + e.getMessage(), e);
    }
  }

  private String readFileContent(File file) throws IOException {
    try {
      // UTF-8で読み込みを試行
      return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    } catch (IOException e) {
      // UTF-8で失敗した場合はシステムデフォルトエンコーディングで試行
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(new FileInputStream(file)))) {
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          content.append(line).append(System.lineSeparator());
        }
        return content.toString();
      }
    }
  }

  private void setupWindow() {
    setTitle("テキストプレビュー - " + textFile.getName());
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout());

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    add(scrollPane, BorderLayout.CENTER);

    // ウィンドウサイズの設定
    setSize(800, 600);
    setLocationRelativeTo(null);

    // ステータスバーの追加（ファイル情報表示）
    JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel statusLabel = new JLabel(String.format("ファイルサイズ: %d bytes | エンコーディング: UTF-8",
        textFile.length()));
    statusPanel.add(statusLabel);
    add(statusPanel, BorderLayout.SOUTH);
  }
}
