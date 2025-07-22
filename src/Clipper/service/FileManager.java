package Clipper.service;

import Clipper.model.ClipboardEntry;
import Clipper.util.CsvUtil;
import Clipper.util.DateUtil;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * ファイル操作を管理するクラス
 * 非同期での保存・読み込みに対応
 */
public class FileManager {

  private final String dataDirectory;
  private final Executor fileOperationExecutor;

  public FileManager() {
    // データディレクトリを設定（~/Documents/ClipperData/）
    String homeDir = System.getProperty("user.home");
    this.dataDirectory = homeDir + File.separator + "Documents" + File.separator + "ClipperData";
    this.fileOperationExecutor = Executors.newSingleThreadExecutor(r -> {
      Thread thread = new Thread(r, "FileManager-Thread");
      thread.setDaemon(true);
      return thread;
    });

    // データディレクトリを作成
    createDataDirectory();
  }

  /**
   * データディレクトリを作成
   */
  private void createDataDirectory() {
    File dir = new File(dataDirectory);
    if (!dir.exists()) {
      if (!dir.mkdirs()) {
        System.err.println("データディレクトリの作成に失敗しました: " + dataDirectory);
      }
    }
  }

  /**
   * 指定された日付のCSVファイルパスを取得
   */
  public String getCsvFilePath(LocalDate date) {
    return dataDirectory + File.separator + DateUtil.generateCsvFileName(date);
  }

  /**
   * エントリを非同期で保存
   */
  public CompletableFuture<Boolean> saveEntryAsync(ClipboardEntry entry) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        LocalDate date = entry.getTimestamp().toLocalDate();
        String filePath = getCsvFilePath(date);

        // ヘッダーの追加が必要かチェック
        boolean needsHeader = !CsvUtil.isValidCsvFile(filePath);

        if (needsHeader) {
          // 新規ファイルの場合はヘッダーを追加
          List<String[]> rows = new ArrayList<>();
          rows.add(CsvUtil.createCsvHeader());
          rows.add(entry.toCsvArray());
          CsvUtil.writeCsvFile(filePath, rows);
        } else {
          // 既存ファイルに追記
          CsvUtil.appendCsvLine(filePath, entry.toCsvArray());
        }

        return true;
      } catch (IOException e) {
        System.err.println("エントリの保存に失敗しました: " + e.getMessage());
        return false;
      }
    }, fileOperationExecutor);
  }

  /**
   * 指定された日付のエントリを読み込み
   */
  public CompletableFuture<List<ClipboardEntry>> loadEntriesAsync(LocalDate date) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        String filePath = getCsvFilePath(date);

        if (!CsvUtil.isValidCsvFile(filePath)) {
          return new ArrayList<>();
        }

        List<String[]> rows = CsvUtil.readCsvFile(filePath);
        List<ClipboardEntry> entries = new ArrayList<>();

        // ヘッダー行をスキップ
        for (int i = 1; i < rows.size(); i++) {
          String[] row = rows.get(i);
          if (row.length >= 5) {
            try {
              String id = row[0];
              LocalDateTime timestamp = DateUtil.parseTimestamp(row[1]);
              boolean isFavorite = Boolean.parseBoolean(row[2]);
              // row[3] はカテゴリ（現在未使用）
              String text = row[4];

              ClipboardEntry entry = new ClipboardEntry(id, timestamp, isFavorite, text);
              entries.add(entry);
            } catch (Exception e) {
              System.err.println("エントリの解析に失敗しました: " + e.getMessage());
            }
          }
        }

        return entries;
      } catch (IOException e) {
        System.err.println("ファイルの読み込みに失敗しました: " + e.getMessage());
        return new ArrayList<>();
      }
    }, fileOperationExecutor);
  }

  /**
   * 最近のデータを読み込み
   */
  public CompletableFuture<List<ClipboardEntry>> loadRecentEntriesAsync(int days) {
    return CompletableFuture.supplyAsync(() -> {
      List<ClipboardEntry> allEntries = new ArrayList<>();
      List<LocalDate> recentDates = DateUtil.getRecentDates(days);

      for (LocalDate date : recentDates) {
        try {
          List<ClipboardEntry> dayEntries = loadEntriesAsync(date).get();
          allEntries.addAll(dayEntries);
        } catch (Exception e) {
          System.err.println("日付 " + date + " のデータ読み込みに失敗しました: " + e.getMessage());
        }
      }

      // タイムスタンプでソート（新しいものが先頭）
      allEntries.sort((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()));

      return allEntries;
    }, fileOperationExecutor);
  }

  /**
   * エントリを削除（CSVファイルを再書き込み）
   */
  public CompletableFuture<Boolean> deleteEntryAsync(ClipboardEntry entry) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        LocalDate date = entry.getTimestamp().toLocalDate();
        String filePath = getCsvFilePath(date);

        if (!CsvUtil.isValidCsvFile(filePath)) {
          return false;
        }

        // ファイル全体を読み込み
        List<String[]> rows = CsvUtil.readCsvFile(filePath);
        List<String[]> updatedRows = new ArrayList<>();

        // ヘッダーを追加
        if (!rows.isEmpty()) {
          updatedRows.add(rows.get(0));
        }

        // 指定されたエントリ以外を追加
        for (int i = 1; i < rows.size(); i++) {
          String[] row = rows.get(i);
          if (row.length > 0 && !row[0].equals(entry.getId())) {
            updatedRows.add(row);
          }
        }

        // ファイルを再書き込み
        CsvUtil.writeCsvFile(filePath, updatedRows);
        return true;

      } catch (IOException e) {
        System.err.println("エントリの削除に失敗しました: " + e.getMessage());
        return false;
      }
    }, fileOperationExecutor);
  }

  /**
   * エントリの更新（お気に入りフラグなど）
   */
  public CompletableFuture<Boolean> updateEntryAsync(ClipboardEntry entry) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        LocalDate date = entry.getTimestamp().toLocalDate();
        String filePath = getCsvFilePath(date);

        if (!CsvUtil.isValidCsvFile(filePath)) {
          return false;
        }

        // ファイル全体を読み込み
        List<String[]> rows = CsvUtil.readCsvFile(filePath);
        List<String[]> updatedRows = new ArrayList<>();

        // ヘッダーを追加
        if (!rows.isEmpty()) {
          updatedRows.add(rows.get(0));
        }

        // 該当エントリを更新
        boolean found = false;
        for (int i = 1; i < rows.size(); i++) {
          String[] row = rows.get(i);
          if (row.length > 0 && row[0].equals(entry.getId())) {
            updatedRows.add(entry.toCsvArray());
            found = true;
          } else {
            updatedRows.add(row);
          }
        }

        if (found) {
          CsvUtil.writeCsvFile(filePath, updatedRows);
          return true;
        }

        return false;

      } catch (IOException e) {
        System.err.println("エントリの更新に失敗しました: " + e.getMessage());
        return false;
      }
    }, fileOperationExecutor);
  }

  /**
   * データディレクトリ内のすべてのCSVファイルを取得
   */
  public List<LocalDate> getAvailableDataDates() {
    List<LocalDate> dates = new ArrayList<>();
    File dir = new File(dataDirectory);

    if (dir.exists() && dir.isDirectory()) {
      File[] files = dir.listFiles((dir1, name) -> name.endsWith(".csv"));
      if (files != null) {
        for (File file : files) {
          String fileName = file.getName();
          String dateStr = fileName.replace(".csv", "");
          try {
            LocalDate date = DateUtil.parseDateFromFile(dateStr);
            dates.add(date);
          } catch (Exception e) {
            System.err.println("ファイル名の解析に失敗しました: " + fileName);
          }
        }
      }
    }

    // 日付順でソート（新しいものが先頭）
    dates.sort((d1, d2) -> d2.compareTo(d1));
    return dates;
  }

  /**
   * データディレクトリのパスを取得
   */
  public String getDataDirectory() {
    return dataDirectory;
  }

  /**
   * リソースのクリーンアップ
   */
  public void shutdown() {
    if (fileOperationExecutor instanceof java.util.concurrent.ExecutorService) {
      ((java.util.concurrent.ExecutorService) fileOperationExecutor).shutdown();
    }
  }
}
