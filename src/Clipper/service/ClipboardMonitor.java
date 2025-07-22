package Clipper.service;

import Clipper.model.ClipboardData;
import Clipper.model.ClipboardEntry;

import javax.swing.*;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClipboardMonitor implements ClipboardOwner {

  private final ClipboardData clipboardData;
  private final FileManager fileManager;
  private final ScheduledExecutorService scheduler;
  private final AtomicBoolean isMonitoring;
  private final Clipboard systemClipboard;

  private static final int POLLING_INTERVAL_MS = 500;
  private String lastClipboardContent;

  private ClipboardChangeListener changeListener;

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

    initializeLastContent();
  }

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

  public void stopMonitoring() {
    if (!isMonitoring.get()) {
      return;
    }

    isMonitoring.set(false);
    scheduler.shutdown();

    System.out.println("クリップボード監視を停止しました");
  }

  public boolean isMonitoring() {
    return isMonitoring.get();
  }

  private void checkClipboard() {
    if (!isMonitoring.get()) {
      return;
    }

    try {
      if (!systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
        return;
      }

      String currentContent = (String) systemClipboard.getData(DataFlavor.stringFlavor);

      if (currentContent != null &&
          !currentContent.equals(lastClipboardContent) &&
          !currentContent.trim().isEmpty()) {

        handleClipboardChange(currentContent);
        lastClipboardContent = currentContent;
      }

    } catch (UnsupportedFlavorException | java.io.IOException e) {
    } catch (IllegalStateException e) {
      notifyError("クリップボードへのアクセスが拒否されました");
    } catch (Exception e) {
      notifyError("クリップボード監視エラー: " + e.getMessage());
    }
  }

  private void handleClipboardChange(String newContent) {
    System.out.println("クリップボード変更検出: " + newContent.substring(0, Math.min(30, newContent.length())));

    try {
      boolean added = clipboardData.addEntry(newContent);
      if (!added) {
        System.out.println("重複のためスキップ");
        return;
      }

      // 最新のエントリを取得
      ClipboardEntry newEntry = clipboardData.getAllEntries().get(0);

      // リスナーに通知
      notifyClipboardChange(newEntry);

      // 保存処理
      try {
        Boolean result = fileManager.saveEntryAsync(newEntry).get(3, TimeUnit.SECONDS);
        if (result != null && result) {
          System.out.println("保存成功");
        } else {
          System.err.println("保存失敗");
        }
      } catch (TimeoutException e) {
        System.err.println("保存タイムアウト");
      } catch (ExecutionException e) {
        System.err.println("保存実行エラー: " + e.getCause().getMessage());
      } catch (InterruptedException e) {
        System.err.println("保存中断");
        Thread.currentThread().interrupt();
      }

    } catch (Exception e) {
      System.err.println("クリップボード処理エラー: " + e.getMessage());
    }
  }

  private void notifyClipboardChange(ClipboardEntry entry) {
    if (changeListener != null) {
      SwingUtilities.invokeLater(() -> changeListener.onClipboardChanged(entry));
    }
  }

  private void notifyError(String error) {
    if (changeListener != null) {
      SwingUtilities.invokeLater(() -> changeListener.onClipboardError(error));
    }
  }

  public void copyToClipboard(String text) {
    try {
      StringSelection selection = new StringSelection(text);
      systemClipboard.setContents(selection, this);

      lastClipboardContent = text;

    } catch (Exception e) {
      notifyError("クリップボードへのコピーに失敗しました: " + e.getMessage());
    }
  }

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

  public void setChangeListener(ClipboardChangeListener listener) {
    this.changeListener = listener;
  }

  @Override
  public void lostOwnership(Clipboard clipboard, Transferable contents) {
  }

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
