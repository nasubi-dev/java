package oop1.section11;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DataAggregatorApp extends JFrame {

  private JTextField filePathField;
  private JButton selectFileButton;
  private JButton startButton;
  private JButton cancelButton;
  private JProgressBar progressBar;
  private JLabel statusLabel;
  private JTextArea resultArea;
  private JScrollPane scrollPane;

  private DataProcessor currentProcessor;
  private File selectedFile;

  public DataAggregatorApp() {
    setTitle("大規模データ集計アプリケーション");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    initializeComponents();
    setupLayout();
    setupEventHandlers();
    updateButtonStates(false); // 初期状態では処理停止中
    setSize(800, 600);
    setLocationRelativeTo(null);
  }

  private void initializeComponents() {
    // ファイル選択関連
    filePathField = new JTextField(50);
    filePathField.setEditable(false);
    selectFileButton = new JButton("ファイル選択");

    // 操作ボタン
    startButton = new JButton("集計開始");
    cancelButton = new JButton("キャンセル");

    // 進捗表示
    progressBar = new JProgressBar(0, 100);
    progressBar.setStringPainted(true);
    statusLabel = new JLabel("待機中");
    statusLabel.setFont(new Font("Serif", Font.BOLD, 14));

    // 結果表示
    resultArea = new JTextArea(20, 60);
    resultArea.setEditable(false);
    resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    scrollPane = new JScrollPane(resultArea);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
  }

  private void setupLayout() {
    setLayout(new BorderLayout());

    // ファイル選択パネル
    JPanel filePanel = new JPanel(new BorderLayout(10, 10));
    filePanel.setBorder(BorderFactory.createTitledBorder("ファイル選択"));
    filePanel.add(new JLabel("CSVファイル:"), BorderLayout.WEST);
    filePanel.add(filePathField, BorderLayout.CENTER);
    filePanel.add(selectFileButton, BorderLayout.EAST);

    // 操作パネル
    JPanel controlPanel = new JPanel(new FlowLayout());
    controlPanel.add(startButton);
    controlPanel.add(cancelButton);

    // 進捗表示パネル
    JPanel progressPanel = new JPanel(new BorderLayout(10, 10));
    progressPanel.setBorder(BorderFactory.createTitledBorder("進捗状況"));
    progressPanel.add(statusLabel, BorderLayout.NORTH);
    progressPanel.add(progressBar, BorderLayout.CENTER);
    progressPanel.add(controlPanel, BorderLayout.SOUTH);

    // 結果表示パネル
    JPanel resultPanel = new JPanel(new BorderLayout());
    resultPanel.setBorder(BorderFactory.createTitledBorder("集計結果"));
    resultPanel.add(scrollPane, BorderLayout.CENTER);

    // 上部パネル（ファイル選択 + 進捗）
    JPanel topPanel = new JPanel(new BorderLayout(10, 10));
    topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    topPanel.add(filePanel, BorderLayout.NORTH);
    topPanel.add(progressPanel, BorderLayout.CENTER);

    add(topPanel, BorderLayout.NORTH);
    add(resultPanel, BorderLayout.CENTER);
  }

  private void setupEventHandlers() {
    selectFileButton.addActionListener(e -> selectFile());
    startButton.addActionListener(e -> startProcessing());
    cancelButton.addActionListener(e -> cancelProcessing());
  }

  private void selectFile() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
    fileChooser.setCurrentDirectory(new File("./src/11/oop1/section11"));

    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      selectedFile = fileChooser.getSelectedFile();
      filePathField.setText(selectedFile.getAbsolutePath());
      startButton.setEnabled(true);
    }
  }

  private void startProcessing() {
    if (selectedFile == null || !selectedFile.exists()) {
      showErrorDialog("有効なファイルを選択してください。");
      return;
    }

    currentProcessor = new DataProcessor(selectedFile);
    currentProcessor.execute();

    updateButtonStates(true);
    statusLabel.setText("処理を開始しています...");
    progressBar.setValue(0);
    resultArea.setText("");
  }

  private void cancelProcessing() {
    if (currentProcessor != null && !currentProcessor.isDone()) {
      currentProcessor.cancel(true);
    }
  }

  private void updateButtonStates(boolean processing) {
    selectFileButton.setEnabled(!processing);
    startButton.setEnabled(!processing && selectedFile != null);
    cancelButton.setEnabled(processing);
  }

  private void showErrorDialog(String message) {
    JOptionPane.showMessageDialog(this, message, "エラー", JOptionPane.ERROR_MESSAGE);
  }

  private class DataProcessor extends SwingWorker<Map<String, SummaryData>, Integer> {
    private final File file;
    private int totalLines = 0;

    public DataProcessor(File file) {
      this.file = file;
    }

    @Override
    protected Map<String, SummaryData> doInBackground() throws Exception {
      Map<String, SummaryData> categoryData = new HashMap<>();

      setProgress(0);
      publish(0);
      totalLines = countLines();

      if (isCancelled())
        return null;

      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        String line = reader.readLine(); // ヘッダー行をスキップ
        int processedLines = 0;

        while ((line = reader.readLine()) != null && !isCancelled()) {
          processLine(line, categoryData);
          processedLines++;

          if (processedLines % 1000 == 0) {
            int progress = (int) ((double) processedLines / totalLines * 100);
            setProgress(progress);
            publish(progress);
          }
        }

        if (!isCancelled()) {
          setProgress(100);
          publish(100);
        }
      }

      return categoryData;
    }

    private int countLines() throws IOException {
      int lines = 0;
      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        while (reader.readLine() != null && !isCancelled()) {
          lines++;
        }
      }
      return lines - 1; // ヘッダー行を除く
    }

    private void processLine(String line, Map<String, SummaryData> categoryData) {
      try {
        String[] parts = line.split(",");
        if (parts.length >= 5) {
          String category = parts[2].trim();
          int price = Integer.parseInt(parts[3].trim());
          int quantity = Integer.parseInt(parts[4].trim());

          categoryData.computeIfAbsent(category, k -> new SummaryData())
              .addTransaction(price, quantity);
        }
      } catch (NumberFormatException e) {
        // 不正なデータ行は無視
      }
    }

    @Override
    protected void process(List<Integer> chunks) {
      // 最新の進捗値を取得
      Integer latestProgress = chunks.get(chunks.size() - 1);
      progressBar.setValue(latestProgress);

      if (latestProgress == 0) {
        statusLabel.setText("ファイルの行数を確認中...");
      } else if (latestProgress < 100) {
        statusLabel.setText(String.format("データを集計中... %d%%", latestProgress));
      } else {
        statusLabel.setText("集計処理を完了しています...");
      }
    }

    @Override
    protected void done() {
      updateButtonStates(false);

      try {
        if (isCancelled()) {
          statusLabel.setText("キャンセルされました");
          resultArea.setText("処理がキャンセルされました。");
        } else {
          Map<String, SummaryData> results = get();
          statusLabel.setText("完了");
          displayResults(results);
        }
      } catch (InterruptedException e) {
        statusLabel.setText("処理が中断されました");
        showErrorDialog("処理が中断されました: " + e.getMessage());
      } catch (ExecutionException e) {
        statusLabel.setText("エラーが発生しました");
        showErrorDialog("処理中にエラーが発生しました: " + e.getCause().getMessage());
      }
    }

    private void displayResults(Map<String, SummaryData> results) {
      StringBuilder sb = new StringBuilder();
      sb.append("=== 商品カテゴリ別 売上集計結果 ===\n\n");

      long grandTotal = 0;
      int totalTransactions = 0;

      for (Map.Entry<String, SummaryData> entry : results.entrySet()) {
        String category = entry.getKey();
        SummaryData data = entry.getValue();

        sb.append(String.format("【%s】\n", category));
        sb.append(String.format("  %s\n\n", data.toString()));

        grandTotal += data.getTotalSales();
        totalTransactions += data.getTransactionCount();
      }

      sb.append("=== 全体サマリー ===\n");
      sb.append(String.format("総売上高: %,d円\n", grandTotal));
      sb.append(String.format("総取引数: %,d件\n", totalTransactions));
      sb.append(String.format("全体平均単価: %.2f円\n",
          totalTransactions > 0 ? (double) grandTotal / totalTransactions : 0.0));

      resultArea.setText(sb.toString());
      resultArea.setCaretPosition(0);
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      new DataAggregatorApp().setVisible(true);
    });
  }
}
