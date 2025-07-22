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

/**
 * Clipperアプリケーションのメインウィンドウ
 */
public class MainWindow extends JFrame implements
    SideBarPanel.DateSelectionListener,
    ClipboardMonitor.ClipboardChangeListener {

  private final ClipboardData clipboardData;
  private final ClipboardMonitor clipboardMonitor;
  private final FileManager fileManager;

  // UIコンポーネント
  private SideBarPanel sideBarPanel;
  private HistoryPanel historyPanel;
  private JLabel monitoringStatusLabel;
  private JMenuBar menuBar;

  // 設定
  private static final String WINDOW_TITLE = "Clipper - クリップボード管理";
  private static final int DEFAULT_WIDTH = 900;
  private static final int DEFAULT_HEIGHT = 600;

  public MainWindow() {
    // モデルとサービスの初期化
    this.clipboardData = new ClipboardData();
    this.fileManager = new FileManager();
    this.clipboardMonitor = new ClipboardMonitor(clipboardData, fileManager);

    // UIの初期化
    initializeUI();
    loadInitialData();
    setupEventHandlers();

    // クリップボード監視の開始
    startClipboardMonitoring();
  }

  /**
   * UIコンポーネントを初期化
   */
  private void initializeUI() {
    setTitle(WINDOW_TITLE);
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    setLocationRelativeTo(null);

    // アプリケーションアイコン（必要に応じて設定）
    // setIconImage(...);

    // メニューバーの作成
    createMenuBar();

    // メインレイアウト
    setLayout(new BorderLayout());

    // サイドバー
    sideBarPanel = new SideBarPanel(clipboardData, fileManager, this);
    add(sideBarPanel, BorderLayout.WEST);

    // 履歴パネル
    historyPanel = new HistoryPanel(clipboardData, clipboardMonitor, fileManager);
    add(historyPanel, BorderLayout.CENTER);

    // ステータスバー
    JPanel statusPanel = createStatusPanel();
    add(statusPanel, BorderLayout.SOUTH);

    // Look and Feelの設定
    try {
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
      // SwingUtilities.updateComponentTreeUI(this);
      System.out.println("Look and Feel設定をスキップしました");
    } catch (Exception e) {
      System.err.println("Look and Feelの設定に失敗: " + e.getMessage());
    }
  }

  /**
   * メニューバーを作成
   */
  private void createMenuBar() {
    menuBar = new JMenuBar();

    // ファイルメニュー
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

    // 編集メニュー
    JMenu editMenu = new JMenu("編集");

    JMenuItem clearHistoryItem = new JMenuItem("履歴をクリア");
    clearHistoryItem.addActionListener(e -> clearHistory());
    editMenu.add(clearHistoryItem);

    JMenuItem preferencesItem = new JMenuItem("設定...");
    preferencesItem.addActionListener(e -> showPreferences());
    editMenu.add(preferencesItem);

    // ヘルプメニュー
    JMenu helpMenu = new JMenu("ヘルプ");

    JMenuItem aboutItem = new JMenuItem("Clipperについて");
    aboutItem.addActionListener(e -> showAbout());
    helpMenu.add(aboutItem);

    menuBar.add(fileMenu);
    menuBar.add(editMenu);
    menuBar.add(helpMenu);

    setJMenuBar(menuBar);
  }

  /**
   * ステータスパネルを作成
   */
  private JPanel createStatusPanel() {
    JPanel statusPanel = new JPanel(new BorderLayout());
    statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
    statusPanel.setBackground(new Color(248, 248, 248));

    // 左側：監視状態
    monitoringStatusLabel = new JLabel("クリップボード監視: 停止中");
    monitoringStatusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    monitoringStatusLabel.setFont(monitoringStatusLabel.getFont().deriveFont(Font.PLAIN, 11f));
    statusPanel.add(monitoringStatusLabel, BorderLayout.WEST);

    // 右側：データディレクトリ情報
    JLabel dataPathLabel = new JLabel("データ保存場所: " + fileManager.getDataDirectory());
    dataPathLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    dataPathLabel.setFont(dataPathLabel.getFont().deriveFont(Font.PLAIN, 11f));
    dataPathLabel.setForeground(Color.GRAY);
    statusPanel.add(dataPathLabel, BorderLayout.EAST);

    return statusPanel;
  }

  /**
   * 初期データを読み込み
   */
  private void loadInitialData() {
    // 最近7日間のデータを読み込み
    fileManager.loadRecentEntriesAsync(7)
        .thenAccept(entries -> {
          clipboardData.loadEntries(entries);
          SwingUtilities.invokeLater(() -> {
            historyPanel.loadTodayEntries();
            sideBarPanel.refresh();
          });
        })
        .exceptionally(throwable -> {
          System.err.println("初期データの読み込みに失敗: " + throwable.getMessage());
          return null;
        });
  }

  /**
   * イベントハンドラーを設定
   */
  private void setupEventHandlers() {
    // ウィンドウクローズイベント
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        exitApplication();
      }
    });

    // クリップボード監視のリスナー設定
    clipboardMonitor.setChangeListener(this);
  }

  /**
   * クリップボード監視を開始
   */
  private void startClipboardMonitoring() {
    clipboardMonitor.startMonitoring();
    updateMonitoringStatus();
  }

  /**
   * 監視状態を更新
   */
  private void updateMonitoringStatus() {
    boolean isMonitoring = clipboardMonitor.isMonitoring();
    monitoringStatusLabel.setText("クリップボード監視: " + (isMonitoring ? "実行中" : "停止中"));
    monitoringStatusLabel.setForeground(isMonitoring ? new Color(0, 128, 0) : Color.RED);
  }

  // SideBarPanel.DateSelectionListener の実装

  @Override
  public void onDateSelected(LocalDate date) {
    historyPanel.loadEntriesForDate(date);
  }

  @Override
  public void onFavoritesSelected() {
    // お気に入りのみを表示
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

  // ClipboardMonitor.ClipboardChangeListener の実装

  @Override
  public void onClipboardChanged(ClipboardEntry newEntry) {
    // 新しいエントリが追加された際の処理
    historyPanel.onNewEntry(newEntry);
    sideBarPanel.refresh();
  }

  @Override
  public void onClipboardError(String error) {
    // エラー通知をステータスバーに表示
    JOptionPane.showMessageDialog(this,
        "クリップボード監視エラー: " + error,
        "エラー",
        JOptionPane.ERROR_MESSAGE);
  }

  // メニューアクション

  private void exportData() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("データをエクスポート");
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      // エクスポート処理の実装（将来の拡張）
      JOptionPane.showMessageDialog(this, "エクスポート機能は実装中です。");
    }
  }

  private void importData() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("データをインポート");
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      // インポート処理の実装（将来の拡張）
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
      clipboardData.clear();
      historyPanel.loadTodayEntries();
      sideBarPanel.refresh();
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

  /**
   * アプリケーションを終了
   */
  private void exitApplication() {
    int result = JOptionPane.showConfirmDialog(this,
        "Clipperを終了しますか？",
        "終了確認",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);

    if (result == JOptionPane.YES_OPTION) {
      // リソースのクリーンアップ
      clipboardMonitor.shutdown();
      fileManager.shutdown();

      // アプリケーション終了
      System.exit(0);
    }
  }

  /**
   * ウィンドウを表示
   */
  public void showWindow() {
    setVisible(true);
  }
}
