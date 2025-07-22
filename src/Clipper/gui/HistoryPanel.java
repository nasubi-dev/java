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

public class HistoryPanel extends JPanel implements EntryPanel.EntryActionListener {

  private final ClipboardData clipboardData;
  private final ClipboardMonitor clipboardMonitor;
  private final FileManager fileManager;

  private JPanel entriesContainer;
  private JScrollPane scrollPane;
  private JLabel statusLabel;
  private JTextField searchField;
  private JButton clearAllButton;

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

  private void initializeUI() {
    setLayout(new BorderLayout());
    setBackground(Color.WHITE);

    JPanel toolbarPanel = createToolbarPanel();
    add(toolbarPanel, BorderLayout.NORTH);

    entriesContainer = new JPanel();
    entriesContainer.setLayout(new BoxLayout(entriesContainer, BoxLayout.Y_AXIS));
    entriesContainer.setBackground(Color.WHITE);

    scrollPane = new JScrollPane(entriesContainer);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    add(scrollPane, BorderLayout.CENTER);

    JPanel statusPanel = createStatusPanel();
    add(statusPanel, BorderLayout.SOUTH);
  }

  private JPanel createToolbarPanel() {
    JPanel toolbarPanel = new JPanel(new BorderLayout());
    toolbarPanel.setBackground(new Color(248, 248, 248));
    toolbarPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
        BorderFactory.createEmptyBorder(8, 8, 8, 8)));

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

  private JPanel createStatusPanel() {
    JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    statusPanel.setBackground(new Color(248, 248, 248));
    statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

    statusLabel = new JLabel("準備中...");
    statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11f));
    statusPanel.add(statusLabel);

    return statusPanel;
  }

  public void loadTodayEntries() {
    loadEntriesForDate(LocalDate.now());
  }

  public void loadEntriesForDate(LocalDate date) {
    currentDisplayDate = date;
    statusLabel.setText("読み込み中...");

    // 単一ファイル対応：メモリ内のデータから日付で絞り込み
    SwingUtilities.invokeLater(() -> {
      List<ClipboardEntry> entries = clipboardData.getEntriesByDate(date);
      displayEntries(entries);
      updateStatusLabel(entries.size());
      System.out.println("日付別表示: " + date + " (" + entries.size() + " エントリ)");
    });
  }

  private void toggleFavoritesOnly() {
    showFavoritesOnly = !showFavoritesOnly;
    refreshDisplay();
  }

  private void performSearch() {
    currentSearchQuery = searchField.getText();
    refreshDisplay();
  }

  private void refreshDisplay() {
    if (currentDisplayDate != null) {
      loadEntriesForDate(currentDisplayDate);
    }
  }

  private void displayEntries(List<ClipboardEntry> entries) {
    entriesContainer.removeAll();

    List<ClipboardEntry> filteredEntries = entries.stream()
        .filter(entry -> {

          if (showFavoritesOnly && !entry.isFavorite()) {
            return false;
          }

          if (!currentSearchQuery.isEmpty()) {
            return entry.getText().toLowerCase().contains(currentSearchQuery.toLowerCase());
          }

          return true;
        })
        .collect(java.util.stream.Collectors.toList());

    if (filteredEntries.isEmpty()) {

      JLabel emptyLabel = new JLabel("エントリが見つかりません");
      emptyLabel.setForeground(Color.GRAY);
      emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
      emptyLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
      entriesContainer.add(emptyLabel);
    } else {

      for (ClipboardEntry entry : filteredEntries) {
        EntryPanel entryPanel = new EntryPanel(entry, clipboardMonitor, this);
        entriesContainer.add(entryPanel);
        entriesContainer.add(Box.createRigidArea(new Dimension(0, 5)));
      }
    }

    entriesContainer.revalidate();
    entriesContainer.repaint();

    SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
  }

  private void updateStatusLabel(int totalCount) {
    String dateStr = currentDisplayDate.equals(LocalDate.now()) ? "今日" : currentDisplayDate.toString();
    statusLabel.setText(String.format("%s - %d件のエントリ", dateStr, totalCount));
  }

  public void displayFavoriteEntries() {
    List<ClipboardEntry> favoriteEntries = clipboardData.getFavoriteEntries();
    displayEntries(favoriteEntries);
    statusLabel.setText("お気に入り - " + favoriteEntries.size() + "件のエントリ");
  }

  public void displayAllEntries() {
    List<ClipboardEntry> allEntries = clipboardData.getAllEntries();
    displayEntries(allEntries);
    statusLabel.setText("すべて - " + allEntries.size() + "件のエントリ");
  }

  private void clearAllEntries() {
    int result = JOptionPane.showConfirmDialog(
        this,
        "すべてのエントリを削除しますか？\nこの操作は元に戻せません。",
        "全削除確認",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);

    if (result == JOptionPane.YES_OPTION) {
      // メモリ上のデータをクリア
      clipboardData.clear();

      // ファイルもクリア
      fileManager.clearAllEntriesAsync()
          .thenAccept(success -> {
            SwingUtilities.invokeLater(() -> {
              if (success) {
                System.out.println("すべてのエントリを削除しました");
                displayEntries(clipboardData.getAllEntries());
                updateStatusLabel(0);
              } else {
                JOptionPane.showMessageDialog(this,
                    "ファイルの削除に失敗しました。",
                    "エラー",
                    JOptionPane.ERROR_MESSAGE);
              }
            });
          })
          .exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
              JOptionPane.showMessageDialog(this,
                  "削除処理中にエラーが発生しました: " + throwable.getMessage(),
                  "エラー",
                  JOptionPane.ERROR_MESSAGE);
            });
            throwable.printStackTrace();
            return null;
          });
    }
  }

  public void onNewEntry(ClipboardEntry entry) {
    if (currentDisplayDate != null && currentDisplayDate.equals(LocalDate.now())) {
      refreshDisplay();
    }
  }

  @Override
  public void onEntryDeleted(ClipboardEntry entry) {
    // メモリ上のデータから削除
    boolean memoryDeleted = clipboardData.removeEntry(entry.getId());
    
    if (memoryDeleted) {
      // ファイルからも削除
      fileManager.deleteEntryAsync(entry)
          .thenAccept(success -> {
            SwingUtilities.invokeLater(() -> {
              if (success) {
                System.out.println("エントリを削除しました: " + entry.getId());
                // UI更新は既にrefreshDisplay()で実行済み
              } else {
                // ファイル削除に失敗した場合、メモリからも復元する
                clipboardData.restoreEntry(entry);
                refreshDisplay();
                JOptionPane.showMessageDialog(this,
                    "ファイルからの削除に失敗しました。",
                    "削除エラー",
                    JOptionPane.ERROR_MESSAGE);
              }
            });
          })
          .exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
              // エラー時はメモリからも復元
              clipboardData.restoreEntry(entry);
              refreshDisplay();
              JOptionPane.showMessageDialog(this,
                  "削除処理中にエラーが発生しました: " + throwable.getMessage(),
                  "削除エラー",
                  JOptionPane.ERROR_MESSAGE);
            });
            throwable.printStackTrace();
            return null;
          });
      
      // UI を即座に更新（楽観的更新）
      refreshDisplay();
    } else {
      JOptionPane.showMessageDialog(this,
          "エントリが見つかりません。",
          "削除エラー",
          JOptionPane.ERROR_MESSAGE);
    }
  }  @Override
  public void onEntryFavoriteToggled(ClipboardEntry entry) {

    clipboardData.toggleFavorite(entry.getId());

    fileManager.updateEntryAsync(entry)
        .thenAccept(success -> {
          if (!success) {
            System.err.println("エントリの更新に失敗: " + entry.getId());
          }
        });
  }

  @Override
  public void onEntryCopied(ClipboardEntry entry) {

    statusLabel.setText("コピーしました: " + entry.getPreviewText());

    Timer timer = new Timer(2000, e -> updateStatusLabel(clipboardData.size()));
    timer.setRepeats(false);
    timer.start();
  }

  public void clearSearch() {
    searchField.setText("");
    currentSearchQuery = "";
    refreshDisplay();
  }

  public LocalDate getCurrentDisplayDate() {
    return currentDisplayDate;
  }
}
