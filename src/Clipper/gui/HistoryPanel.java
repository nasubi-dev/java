package Clipper.gui;

import Clipper.model.ClipboardData;
import Clipper.model.ClipboardEntry;
import Clipper.service.ClipboardMonitor;
import Clipper.service.FileManager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * クリップボード履歴を表示するメインパネル
 */
public class HistoryPanel extends JPanel implements EntryPanel.EntryActionListener {

  private final ClipboardData clipboardData;
  private final ClipboardMonitor clipboardMonitor;
  private final FileManager fileManager;

  // UIコンポーネント
  private JPanel entriesContainer;
  private JScrollPane scrollPane;
  private JLabel statusLabel;
  private JTextField searchField;
  private JButton clearAllButton;

  // 現在の表示状態
  private LocalDate currentDisplayDate;
  private String currentSearchQuery = "";
  private boolean showFavoritesOnly = false;

  public HistoryPanel(ClipboardData clipboardData, ClipboardMonitor clipboardMonitor, FileManager fileManager) {
    this.clipboardData = clipboardData;
    this.clipboardMonitor = clipboardMonitor;
    this.fileManager = fileManager;

    initializeUI();
    loadTodayEntries();
  }

  /**
   * UIコンポーネントを初期化
   */
  private void initializeUI() {
    setLayout(new BorderLayout());
    setBackground(Color.WHITE);

    // ツールバー
    JPanel toolbarPanel = createToolbarPanel();
    add(toolbarPanel, BorderLayout.NORTH);

    // エントリコンテナ
    entriesContainer = new JPanel();
    entriesContainer.setLayout(new BoxLayout(entriesContainer, BoxLayout.Y_AXIS));
    entriesContainer.setBackground(Color.WHITE);

    // スクロールペイン
    scrollPane = new JScrollPane(entriesContainer);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    add(scrollPane, BorderLayout.CENTER);

    // ステータスバー
    JPanel statusPanel = createStatusPanel();
    add(statusPanel, BorderLayout.SOUTH);
  }

  /**
   * ツールバーパネルを作成
   */
  private JPanel createToolbarPanel() {
    JPanel toolbarPanel = new JPanel(new BorderLayout());
    toolbarPanel.setBackground(new Color(248, 248, 248));
    toolbarPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
        BorderFactory.createEmptyBorder(8, 8, 8, 8)));

    // 検索フィールド
    JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    searchPanel.setOpaque(false);

    searchField = new JTextField(20);
    searchField.setToolTipText("テキストを検索...");
    searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
      @Override
      public void insertUpdate(javax.swing.event.DocumentEvent e) {
        performSearch();
      }

      @Override
      public void removeUpdate(javax.swing.event.DocumentEvent e) {
        performSearch();
      }

      @Override
      public void changedUpdate(javax.swing.event.DocumentEvent e) {
        performSearch();
      }
    });

    searchPanel.add(new JLabel("検索:"));
    searchPanel.add(searchField);

    // 操作ボタンパネル
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.setOpaque(false);

    JButton favoritesButton = new JButton("お気に入りのみ");
    favoritesButton.addActionListener(e -> toggleFavoritesOnly());

    clearAllButton = new JButton("すべてクリア");
    clearAllButton.addActionListener(e -> clearAllEntries());
    clearAllButton.setForeground(Color.RED);

    buttonPanel.add(favoritesButton);
    buttonPanel.add(clearAllButton);

    toolbarPanel.add(searchPanel, BorderLayout.WEST);
    toolbarPanel.add(buttonPanel, BorderLayout.EAST);

    return toolbarPanel;
  }

  /**
   * ステータスパネルを作成
   */
  private JPanel createStatusPanel() {
    JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    statusPanel.setBackground(new Color(248, 248, 248));
    statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

    statusLabel = new JLabel("準備中...");
    statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11f));
    statusPanel.add(statusLabel);

    return statusPanel;
  }

  /**
   * 今日のエントリを読み込み
   */
  public void loadTodayEntries() {
    loadEntriesForDate(LocalDate.now());
  }

  /**
   * 指定された日付のエントリを読み込み
   */
  public void loadEntriesForDate(LocalDate date) {
    currentDisplayDate = date;
    statusLabel.setText("読み込み中...");

    CompletableFuture<List<ClipboardEntry>> loadFuture;

    if (date.equals(LocalDate.now())) {
      // 今日の場合は、メモリ上のデータを使用
      loadFuture = CompletableFuture.completedFuture(clipboardData.getEntriesByDate(date));
    } else {
      // 過去の日付の場合は、ファイルから読み込み
      loadFuture = fileManager.loadEntriesAsync(date);
    }

    loadFuture.thenAccept(entries -> {
      SwingUtilities.invokeLater(() -> {
        displayEntries(entries);
        updateStatusLabel(entries.size());
      });
    });
  }

  /**
   * お気に入りのみ表示を切り替え
   */
  private void toggleFavoritesOnly() {
    showFavoritesOnly = !showFavoritesOnly;
    refreshDisplay();
  }

  /**
   * 検索を実行
   */
  private void performSearch() {
    currentSearchQuery = searchField.getText();
    refreshDisplay();
  }

  /**
   * 表示を更新
   */
  private void refreshDisplay() {
    if (currentDisplayDate != null) {
      loadEntriesForDate(currentDisplayDate);
    }
  }

  /**
   * エントリリストを表示
   */
  private void displayEntries(List<ClipboardEntry> entries) {
    entriesContainer.removeAll();

    // フィルタリング
    List<ClipboardEntry> filteredEntries = entries.stream()
        .filter(entry -> {
          // お気に入りフィルタ
          if (showFavoritesOnly && !entry.isFavorite()) {
            return false;
          }

          // 検索フィルタ
          if (!currentSearchQuery.isEmpty()) {
            return entry.getText().toLowerCase().contains(currentSearchQuery.toLowerCase());
          }

          return true;
        })
        .collect(java.util.stream.Collectors.toList());

    if (filteredEntries.isEmpty()) {
      // 空の状態を表示
      JLabel emptyLabel = new JLabel("エントリが見つかりません");
      emptyLabel.setForeground(Color.GRAY);
      emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
      emptyLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
      entriesContainer.add(emptyLabel);
    } else {
      // エントリパネルを作成して追加
      for (ClipboardEntry entry : filteredEntries) {
        EntryPanel entryPanel = new EntryPanel(entry, clipboardMonitor, this);
        entriesContainer.add(entryPanel);
        entriesContainer.add(Box.createRigidArea(new Dimension(0, 5))); // スペース
      }
    }

    entriesContainer.revalidate();
    entriesContainer.repaint();

    // スクロールを最上部に移動
    SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
  }

  /**
   * ステータスラベルを更新
   */
  private void updateStatusLabel(int totalCount) {
    String dateStr = currentDisplayDate.equals(LocalDate.now()) ? "今日" : currentDisplayDate.toString();
    statusLabel.setText(String.format("%s - %d件のエントリ", dateStr, totalCount));
  }

  /**
   * お気に入りエントリのみを表示
   */
  public void displayFavoriteEntries() {
    List<ClipboardEntry> favoriteEntries = clipboardData.getFavoriteEntries();
    displayEntries(favoriteEntries);
    statusLabel.setText("お気に入り - " + favoriteEntries.size() + "件のエントリ");
  }

  /**
   * すべてのエントリを表示
   */
  public void displayAllEntries() {
    List<ClipboardEntry> allEntries = clipboardData.getAllEntries();
    displayEntries(allEntries);
    statusLabel.setText("すべて - " + allEntries.size() + "件のエントリ");
  }

  /**
   * すべてのエントリをクリア
   */
  private void clearAllEntries() {
    int result = JOptionPane.showConfirmDialog(
        this,
        "すべてのエントリを削除しますか？\nこの操作は元に戻せません。",
        "全削除確認",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);

    if (result == JOptionPane.YES_OPTION) {
      clipboardData.clear();
      displayEntries(clipboardData.getAllEntries());
      updateStatusLabel(0);
    }
  }

  /**
   * 新しいエントリが追加された際の処理
   */
  public void onNewEntry(ClipboardEntry entry) {
    if (currentDisplayDate != null && currentDisplayDate.equals(LocalDate.now())) {
      refreshDisplay();
    }
  }

  // EntryActionListener の実装

  @Override
  public void onEntryDeleted(ClipboardEntry entry) {
    // データモデルから削除
    clipboardData.removeEntry(entry.getId());

    // ファイルからも削除
    fileManager.deleteEntryAsync(entry)
        .thenAccept(success -> {
          if (!success) {
            System.err.println("ファイルからのエントリ削除に失敗: " + entry.getId());
          }
        });

    // 表示を更新
    refreshDisplay();
  }

  @Override
  public void onEntryFavoriteToggled(ClipboardEntry entry) {
    // データモデルでお気に入りを切り替え
    clipboardData.toggleFavorite(entry.getId());

    // ファイルを更新
    fileManager.updateEntryAsync(entry)
        .thenAccept(success -> {
          if (!success) {
            System.err.println("エントリの更新に失敗: " + entry.getId());
          }
        });
  }

  @Override
  public void onEntryCopied(ClipboardEntry entry) {
    // 必要に応じてステータス更新など
    statusLabel.setText("コピーしました: " + entry.getPreviewText());

    // 数秒後にステータスを元に戻す
    Timer timer = new Timer(2000, e -> updateStatusLabel(clipboardData.size()));
    timer.setRepeats(false);
    timer.start();
  }

  /**
   * 検索フィールドをクリア
   */
  public void clearSearch() {
    searchField.setText("");
    currentSearchQuery = "";
    refreshDisplay();
  }

  /**
   * 現在表示中の日付を取得
   */
  public LocalDate getCurrentDisplayDate() {
    return currentDisplayDate;
  }
}
