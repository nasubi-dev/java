package Clipper.gui;

import Clipper.model.ClipboardData;
import Clipper.model.ClipboardEntry;
import Clipper.service.ClipboardMonitor;
import Clipper.service.FileManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;

public class MainWindow extends JFrame implements
    SideBarPanel.DateSelectionListener,
    ClipboardMonitor.ClipboardChangeListener {

  private final ClipboardData clipboardData;
  private final ClipboardMonitor clipboardMonitor;
  private final FileManager fileManager;

  private SideBarPanel sideBarPanel;
  private HistoryPanel historyPanel;
  private JLabel monitoringStatusLabel;
  private JMenuBar menuBar;

  private static final String WINDOW_TITLE = "Clipper - クリップボード管理";
  private static final int DEFAULT_WIDTH = 900;
  private static final int DEFAULT_HEIGHT = 600;

  public MainWindow() {

    this.clipboardData = new ClipboardData();
    this.fileManager = new FileManager();
    this.clipboardMonitor = new ClipboardMonitor(clipboardData, fileManager);

    initializeUI();
    loadInitialData();
    setupEventHandlers();

    startClipboardMonitoring();
  }

  private void initializeUI() {
    setTitle(WINDOW_TITLE);
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    setLocationRelativeTo(null);

    createMenuBar();

    setLayout(new BorderLayout());

    sideBarPanel = new SideBarPanel(clipboardData, fileManager, this);
    add(sideBarPanel, BorderLayout.WEST);

    historyPanel = new HistoryPanel(clipboardData, clipboardMonitor, fileManager);
    add(historyPanel, BorderLayout.CENTER);

    JPanel statusPanel = createStatusPanel();
    add(statusPanel, BorderLayout.SOUTH);

    try {

      System.out.println("Look and Feel設定をスキップしました");
    } catch (Exception e) {
      System.err.println("Look and Feelの設定に失敗: " + e.getMessage());
    }
  }

  private void createMenuBar() {
    menuBar = new JMenuBar();

    JMenu fileMenu = new JMenu("ファイル");

    JMenuItem exportItem = new JMenuItem("エクスポート...");
    exportItem.addActionListener(e -> exportData());
    fileMenu.add(exportItem);

    JMenuItem importItem = new JMenuItem("インポート...");
    importItem.addActionListener(e -> importData());
    fileMenu.add(importItem);

    fileMenu.addSeparator();

    JMenuItem exitItem = new JMenuItem("終了");
    exitItem.addActionListener(e -> exitApplication());
    fileMenu.add(exitItem);

    JMenu editMenu = new JMenu("編集");

    JMenuItem clearHistoryItem = new JMenuItem("履歴をクリア");
    clearHistoryItem.addActionListener(e -> clearHistory());
    editMenu.add(clearHistoryItem);

    JMenuItem preferencesItem = new JMenuItem("設定...");
    preferencesItem.addActionListener(e -> showPreferences());
    editMenu.add(preferencesItem);

    JMenu helpMenu = new JMenu("ヘルプ");

    JMenuItem aboutItem = new JMenuItem("Clipperについて");
    aboutItem.addActionListener(e -> showAbout());
    helpMenu.add(aboutItem);

    menuBar.add(fileMenu);
    menuBar.add(editMenu);
    menuBar.add(helpMenu);

    setJMenuBar(menuBar);
  }

  private JPanel createStatusPanel() {
    JPanel statusPanel = new JPanel(new BorderLayout());
    statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
    statusPanel.setBackground(new Color(248, 248, 248));

    monitoringStatusLabel = new JLabel("クリップボード監視: 停止中");
    monitoringStatusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    monitoringStatusLabel.setFont(monitoringStatusLabel.getFont().deriveFont(Font.PLAIN, 11f));
    statusPanel.add(monitoringStatusLabel, BorderLayout.WEST);

    JLabel dataPathLabel = new JLabel("データ保存場所: " + fileManager.getDataDirectory());
    dataPathLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    dataPathLabel.setFont(dataPathLabel.getFont().deriveFont(Font.PLAIN, 11f));
    dataPathLabel.setForeground(Color.GRAY);
    statusPanel.add(dataPathLabel, BorderLayout.EAST);

    return statusPanel;
  }

  private void loadInitialData() {
    System.out.println("初期データの読み込みを開始...");

    fileManager.loadRecentEntriesAsync(7)
        .thenAccept(entries -> {
          System.out.println("読み込み完了エントリ数: " + entries.size());
          clipboardData.loadEntries(entries);
          SwingUtilities.invokeLater(() -> {
            historyPanel.loadTodayEntries();
            sideBarPanel.refresh();
          });
        })
        .exceptionally(throwable -> {
          System.err.println("初期データの読み込みに失敗: " + throwable.getMessage());
          throwable.printStackTrace();
          return null;
        });
  }

  private void setupEventHandlers() {

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        exitApplication();
      }
    });

    clipboardMonitor.setChangeListener(this);
  }

  private void startClipboardMonitoring() {
    clipboardMonitor.startMonitoring();
    updateMonitoringStatus();
  }

  private void updateMonitoringStatus() {
    boolean isMonitoring = clipboardMonitor.isMonitoring();
    monitoringStatusLabel.setText("クリップボード監視: " + (isMonitoring ? "実行中" : "停止中"));
    monitoringStatusLabel.setForeground(isMonitoring ? new Color(0, 128, 0) : Color.RED);
  }

  @Override
  public void onDateSelected(LocalDate date) {
    historyPanel.loadEntriesForDate(date);
  }

  @Override
  public void onFavoritesSelected() {

    SwingUtilities.invokeLater(() -> {
      historyPanel.displayFavoriteEntries();
    });
  }

  @Override
  public void onAllEntriesSelected() {
    SwingUtilities.invokeLater(() -> {
      historyPanel.displayAllEntries();
    });
  }

  @Override
  public void onClipboardChanged(ClipboardEntry newEntry) {
    // EDTで実行することを保証
    SwingUtilities.invokeLater(() -> {
      // HistoryPanelに新しいエントリを通知
      historyPanel.onNewEntry(newEntry);
      
      // SideBarPanelを更新（統計情報など）
      sideBarPanel.refresh();
      
      // ウィンドウタイトルに一時的な通知を表示
      showClipboardUpdateFeedback();
    });
  }

  @Override
  public void onClipboardError(String error) {

    JOptionPane.showMessageDialog(this,
        "クリップボード監視エラー: " + error,
        "エラー",
        JOptionPane.ERROR_MESSAGE);
  }

  private void exportData() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("データをエクスポート");
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

      JOptionPane.showMessageDialog(this, "エクスポート機能は実装中です。");
    }
  }

  private void importData() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("データをインポート");
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

      JOptionPane.showMessageDialog(this, "インポート機能は実装中です。");
    }
  }

  private void clearHistory() {
    int result = JOptionPane.showConfirmDialog(this,
        "すべての履歴を削除しますか？\nこの操作は元に戻せません。",
        "履歴クリア確認",
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
                System.out.println("すべての履歴を削除しました");
                historyPanel.loadTodayEntries();
                sideBarPanel.refresh();
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

  private void showPreferences() {
    JOptionPane.showMessageDialog(this, "設定画面は実装中です。");
  }

  private void showAbout() {
    String aboutText = "Clipper v1.0\n\n" +
        "クリップボード管理アプリケーション\n" +
        "Java Swingで開発\n\n" +
        "データ保存場所: " + fileManager.getDataDirectory();

    JOptionPane.showMessageDialog(this,
        aboutText,
        "Clipperについて",
        JOptionPane.INFORMATION_MESSAGE);
  }

  private void exitApplication() {
    int result = JOptionPane.showConfirmDialog(this,
        "Clipperを終了しますか？",
        "終了確認",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);

    if (result == JOptionPane.YES_OPTION) {
      // クリップボード監視を停止
      clipboardMonitor.shutdown();

      // ファイル管理サービスを停止
      fileManager.shutdown();

      System.out.println("Clipperを終了しました。");
      System.exit(0);
    }
  }

  private void showClipboardUpdateFeedback() {
    // 元のタイトルを保存
    String originalTitle = getTitle();
    
    // 一時的に更新通知を表示
    setTitle(originalTitle + " - 📋 クリップボード更新！");
    
    // 1.5秒後に元のタイトルに戻す
    Timer feedbackTimer = new Timer(1500, e -> setTitle(originalTitle));
    feedbackTimer.setRepeats(false);
    feedbackTimer.start();
  }

  public void showWindow() {
    setVisible(true);
  }
}
