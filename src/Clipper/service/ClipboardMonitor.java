package Clipper.service;

import Clipper.model.ClipboardData;
import Clipper.model.ClipboardEntry;

import javax.swing.*;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * クリップボードの変更を監視するクラス
 * ポーリング方式でクリップボードの内容をチェック
 */
public class ClipboardMonitor implements ClipboardOwner {

  private final ClipboardData clipboardData;
  private final FileManager fileManager;
  private final ScheduledExecutorService scheduler;
  private final AtomicBoolean isMonitoring;
  private final Clipboard systemClipboard;

  // 監視設定
  private static final int POLLING_INTERVAL_MS = 500; // 500msごとにチェック
  private String lastClipboardContent;

  // イベントリスナー
  private ClipboardChangeListener changeListener;

  /**
   * クリップボード変更通知インターフェース
   */
  public interface ClipboardChangeListener {
    void onClipboardChanged(ClipboardEntry newEntry);

    void onClipboardError(String error);
  }

  public ClipboardMonitor(ClipboardData clipboardData, FileManager fileManager) {
    this.clipboardData = clipboardData;
    this.fileManager = fileManager;
    this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread thread = new Thread(r, "ClipboardMonitor-Thread");
      thread.setDaemon(true);
      return thread;
    });
    this.isMonitoring = new AtomicBoolean(false);
    this.systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    this.lastClipboardContent = "";

    // 初期クリップボード内容を取得
    initializeLastContent();
  }

  /**
   * 初期クリップボード内容を設定
   */
  private void initializeLastContent() {
    try {
      if (systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
        String content = (String) systemClipboard.getData(DataFlavor.stringFlavor);
        if (content != null) {
          lastClipboardContent = content;
        }
      }
    } catch (Exception e) {
      System.err.println("初期クリップボード内容の取得に失敗: " + e.getMessage());
    }
  }

  /**
   * 監視を開始
   */
  public void startMonitoring() {
    if (isMonitoring.get()) {
      return;
    }

    isMonitoring.set(true);

    scheduler.scheduleWithFixedDelay(this::checkClipboard,
        0,
        POLLING_INTERVAL_MS,
        TimeUnit.MILLISECONDS);

    System.out.println("クリップボード監視を開始しました");
  }

  /**
   * 監視を停止
   */
  public void stopMonitoring() {
    if (!isMonitoring.get()) {
      return;
    }

    isMonitoring.set(false);
    scheduler.shutdown();

    System.out.println("クリップボード監視を停止しました");
  }

  /**
   * 現在監視中かどうか
   */
  public boolean isMonitoring() {
    return isMonitoring.get();
  }

  /**
   * クリップボードをチェック（ポーリング処理）
   */
  private void checkClipboard() {
    if (!isMonitoring.get()) {
      return;
    }

    try {
      // クリップボードにテキストが存在するかチェック
      if (!systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
        return;
      }

      String currentContent = (String) systemClipboard.getData(DataFlavor.stringFlavor);

      // 内容が変更されたかチェック
      if (currentContent != null &&
          !currentContent.equals(lastClipboardContent) &&
          !currentContent.trim().isEmpty()) {

        handleClipboardChange(currentContent);
        lastClipboardContent = currentContent;
      }

    } catch (UnsupportedFlavorException | java.io.IOException e) {
      // クリップボードにテキスト以外のデータがある場合は無視
    } catch (IllegalStateException e) {
      // クリップボードがアクセス不可能な状態
      notifyError("クリップボードへのアクセスが拒否されました");
    } catch (Exception e) {
      // その他の予期しないエラー
      notifyError("クリップボード監視エラー: " + e.getMessage());
    }
  }

  /**
   * クリップボード変更を処理
   */
  private void handleClipboardChange(String content) {
    // データモデルに追加
    boolean added = clipboardData.addEntry(content);

    if (added) {
      // 新しく追加されたエントリを取得
      ClipboardEntry newEntry = clipboardData.getAllEntries().stream()
          .filter(entry -> entry.getText().equals(content))
          .findFirst()
          .orElse(null);

      if (newEntry != null) {
        // ファイルに非同期で保存
        fileManager.saveEntryAsync(newEntry)
            .thenAccept(success -> {
              if (!success) {
                System.err.println("エントリの保存に失敗しました: " + newEntry.getId());
              }
            });

        // UIに変更を通知
        notifyClipboardChange(newEntry);
      }
    }
  }

  /**
   * クリップボード変更をリスナーに通知
   */
  private void notifyClipboardChange(ClipboardEntry entry) {
    if (changeListener != null) {
      SwingUtilities.invokeLater(() -> changeListener.onClipboardChanged(entry));
    }
  }

  /**
   * エラーをリスナーに通知
   */
  private void notifyError(String error) {
    if (changeListener != null) {
      SwingUtilities.invokeLater(() -> changeListener.onClipboardError(error));
    }
  }

  /**
   * 指定されたテキストをクリップボードにコピー
   */
  public void copyToClipboard(String text) {
    try {
      StringSelection selection = new StringSelection(text);
      systemClipboard.setContents(selection, this);

      // 一時的に監視を停止して、自分の操作を無視
      lastClipboardContent = text;

    } catch (Exception e) {
      notifyError("クリップボードへのコピーに失敗しました: " + e.getMessage());
    }
  }

  /**
   * 現在のクリップボード内容を取得
   */
  public String getCurrentClipboardContent() {
    try {
      if (systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
        return (String) systemClipboard.getData(DataFlavor.stringFlavor);
      }
    } catch (Exception e) {
      System.err.println("クリップボード内容の取得に失敗: " + e.getMessage());
    }
    return "";
  }

  /**
   * 変更リスナーを設定
   */
  public void setChangeListener(ClipboardChangeListener listener) {
    this.changeListener = listener;
  }

  /**
   * ClipboardOwnerインターフェースの実装
   * 他のアプリケーションがクリップボードを使用した際に呼ばれる
   */
  @Override
  public void lostOwnership(Clipboard clipboard, Transferable contents) {
    // 所有権を失った際の処理
    // 特に何もしない（ポーリングで検出するため）
  }

  /**
   * リソースのクリーンアップ
   */
  public void shutdown() {
    stopMonitoring();

    try {
      if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
