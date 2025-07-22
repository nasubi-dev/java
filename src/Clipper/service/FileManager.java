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

public class FileManager {

  private final String dataDirectory;
  private final Executor fileOperationExecutor;

  public FileManager() {
    String homeDir = System.getProperty("user.home");
    this.dataDirectory = homeDir + File.separator + "Documents" + File.separator + "ClipperData";
    this.fileOperationExecutor = Executors.newSingleThreadExecutor(r -> {
      Thread thread = new Thread(r, "FileManager-Thread");
      thread.setDaemon(true);
      return thread;
    });

    createDataDirectory();
  }

  private void createDataDirectory() {
    File dir = new File(dataDirectory);
    if (!dir.exists()) {
      if (!dir.mkdirs()) {
        System.err.println("データディレクトリの作成に失敗しました: " + dataDirectory);
      }
    }
  }

  public String getCsvFilePath(LocalDate date) {
    return dataDirectory + File.separator + DateUtil.generateCsvFileName(date);
  }

  public String getSingleCsvFilePath() {
    return dataDirectory + File.separator + "clipboard_history.csv";
  }

  public CompletableFuture<Boolean> saveEntryAsync(ClipboardEntry entry) {
    System.out.println("保存開始: " + entry.getText().substring(0, Math.min(20, entry.getText().length())));

    CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
      try {
        String filePath = getSingleCsvFilePath();
        File file = new File(filePath);

        List<String[]> allRows = new ArrayList<>();

        // ヘッダーを最初に追加
        allRows.add(CsvUtil.createCsvHeader());

        // 新しいエントリを2番目に追加（ヘッダーの直下、つまり最新が上）
        allRows.add(entry.toCsvArray());

        // 既存のデータがある場合は読み込んで追加（ヘッダーを除く）
        if (CsvUtil.isValidCsvFile(filePath)) {
          List<String[]> existingRows = CsvUtil.readCsvFile(filePath);
          // ヘッダー行をスキップして既存データを追加
          for (int i = 1; i < existingRows.size(); i++) {
            allRows.add(existingRows.get(i));
          }
        }

        // ファイル全体を書き直し
        CsvUtil.writeCsvFile(filePath, allRows);
        System.out.println("単一ファイル保存完了: " + filePath);

        return true;
      } catch (Exception e) {
        System.err.println("保存失敗: " + e.getMessage());
        e.printStackTrace();
        return false;
      }
    }); // エグゼキュータを一時的に削除してテスト

    return future;
  }

  public CompletableFuture<List<ClipboardEntry>> loadEntriesAsync(LocalDate date) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        String filePath = getSingleCsvFilePath();

        if (!CsvUtil.isValidCsvFile(filePath)) {
          System.out.println("ファイルが存在しません: " + filePath);
          return new ArrayList<>();
        }

        System.out.println("読み込み中のファイル: " + filePath);
        List<String[]> rows = CsvUtil.readCsvFile(filePath);
        List<ClipboardEntry> entries = new ArrayList<>();

        // 最大500行（ヘッダー除く）まで読み込み
        int maxEntries = 500;
        int entriesToRead = Math.min(maxEntries, rows.size() - 1);

        System.out.println("ファイル内容 (全 " + (rows.size() - 1) + " エントリ中、上位 " + entriesToRead + " エントリを読み込み):");

        for (int i = 1; i <= entriesToRead; i++) {
          String[] row = rows.get(i);
          if (row.length >= 5) {
            try {
              String id = row[0];
              LocalDateTime timestamp = DateUtil.parseTimestamp(row[1]);
              boolean isFavorite = Boolean.parseBoolean(row[2]);
              String text = row[4];

              ClipboardEntry entry = new ClipboardEntry(id, timestamp, isFavorite, text);
              entries.add(entry);

              // 内容をプレビュー表示（最初の50文字）
              String preview = text.length() > 50 ? text.substring(0, 50) + "..." : text;
              System.out
                  .println("  - " + timestamp.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")) +
                      ": " + preview.replace("\n", "\\n"));
            } catch (Exception e) {
              System.err.println("エントリの解析に失敗しました: " + e.getMessage());
            }
          }
        }

        System.out.println("読み込み完了: " + entries.size() + " エントリ");
        return entries;
      } catch (IOException e) {
        System.err.println("ファイルの読み込みに失敗しました: " + e.getMessage());
        return new ArrayList<>();
      }
    }); // エグゼキュータを削除してデフォルト使用
  }

  public CompletableFuture<List<ClipboardEntry>> loadRecentEntriesAsync(int days) {
    return CompletableFuture.supplyAsync(() -> {
      System.out.println("単一ファイルから最新 500 エントリを読み込み中...");

      // 単一ファイル対応のため、日付は無視して直接loadEntriesAsyncを呼ぶ
      try {
        List<ClipboardEntry> entries = loadEntriesAsync(LocalDate.now()).get();
        System.out.println("全体で " + entries.size() + " エントリを読み込み完了");
        return entries;
      } catch (Exception e) {
        System.err.println("データ読み込みに失敗しました: " + e.getMessage());
        return new ArrayList<>();
      }
    }); // エグゼキュータを削除してデフォルト使用
  }

  public CompletableFuture<Boolean> deleteEntryAsync(ClipboardEntry entry) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        String filePath = getSingleCsvFilePath();

        if (!CsvUtil.isValidCsvFile(filePath)) {
          return false;
        }

        List<String[]> rows = CsvUtil.readCsvFile(filePath);
        List<String[]> updatedRows = new ArrayList<>();

        // ヘッダー行を保持
        if (!rows.isEmpty()) {
          updatedRows.add(rows.get(0));
        }

        // 指定されたIDのエントリ以外を保持
        boolean entryFound = false;
        for (int i = 1; i < rows.size(); i++) {
          String[] row = rows.get(i);
          if (row.length > 0 && !row[0].equals(entry.getId())) {
            updatedRows.add(row);
          } else if (row.length > 0 && row[0].equals(entry.getId())) {
            entryFound = true;
          }
        }

        if (!entryFound) {
          System.out.println("削除対象のエントリが見つかりませんでした: " + entry.getId());
          return false;
        }

        CsvUtil.writeCsvFile(filePath, updatedRows);
        System.out.println("ファイルからエントリを削除しました: " + entry.getId());
        return true;

      } catch (IOException e) {
        System.err.println("エントリの削除に失敗しました: " + e.getMessage());
        e.printStackTrace();
        return false;
      }
    }, fileOperationExecutor);
  }

  public CompletableFuture<Boolean> updateEntryAsync(ClipboardEntry entry) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        String filePath = getSingleCsvFilePath();

        if (!CsvUtil.isValidCsvFile(filePath)) {
          return false;
        }

        List<String[]> rows = CsvUtil.readCsvFile(filePath);
        List<String[]> updatedRows = new ArrayList<>();

        // ヘッダー行を保持
        if (!rows.isEmpty()) {
          updatedRows.add(rows.get(0));
        }

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
          System.out.println("エントリを更新しました: " + entry.getId());
          return true;
        }

        System.out.println("更新対象のエントリが見つかりませんでした: " + entry.getId());
        return false;

      } catch (IOException e) {
        System.err.println("エントリの更新に失敗しました: " + e.getMessage());
        e.printStackTrace();
        return false;
      }
    }, fileOperationExecutor);
  }

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

    dates.sort((d1, d2) -> d2.compareTo(d1));
    return dates;
  }

  public CompletableFuture<Boolean> clearAllEntriesAsync() {
    return CompletableFuture.supplyAsync(() -> {
      try {
        String csvFilePath = getSingleCsvFilePath();
        File csvFile = new File(csvFilePath);

        if (csvFile.exists()) {
          // ファイルの内容を空にする（ヘッダーのみ残す）
          List<String[]> emptyData = new ArrayList<>();
          emptyData.add(CsvUtil.createCsvHeader());
          CsvUtil.writeCsvFile(csvFilePath, emptyData);
          return true;
        } else {
          // ファイルが存在しない場合は成功とみなす
          return true;
        }
      } catch (Exception e) {
        System.err.println("全エントリ削除中にエラーが発生: " + e.getMessage());
        e.printStackTrace();
        return false;
      }
    }, fileOperationExecutor);
  }

  public String getDataDirectory() {
    return dataDirectory;
  }

  public void shutdown() {
    if (fileOperationExecutor instanceof java.util.concurrent.ExecutorService) {
      ((java.util.concurrent.ExecutorService) fileOperationExecutor).shutdown();
    }
  }
}
