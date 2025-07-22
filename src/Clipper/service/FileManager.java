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

  public CompletableFuture<Boolean> saveEntryAsync(ClipboardEntry entry) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        LocalDate date = entry.getTimestamp().toLocalDate();
        String filePath = getCsvFilePath(date);

        boolean needsHeader = !CsvUtil.isValidCsvFile(filePath);

        if (needsHeader) {
          List<String[]> rows = new ArrayList<>();
          rows.add(CsvUtil.createCsvHeader());
          rows.add(entry.toCsvArray());
          CsvUtil.writeCsvFile(filePath, rows);
        } else {
          CsvUtil.appendCsvLine(filePath, entry.toCsvArray());
        }

        return true;
      } catch (IOException e) {
        System.err.println("エントリの保存に失敗しました: " + e.getMessage());
        return false;
      }
    }, fileOperationExecutor);
  }

  public CompletableFuture<List<ClipboardEntry>> loadEntriesAsync(LocalDate date) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        String filePath = getCsvFilePath(date);

        if (!CsvUtil.isValidCsvFile(filePath)) {
          return new ArrayList<>();
        }

        List<String[]> rows = CsvUtil.readCsvFile(filePath);
        List<ClipboardEntry> entries = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
          String[] row = rows.get(i);
          if (row.length >= 5) {
            try {
              String id = row[0];
              LocalDateTime timestamp = DateUtil.parseTimestamp(row[1]);
              boolean isFavorite = Boolean.parseBoolean(row[2]);
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

      allEntries.sort((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()));

      return allEntries;
    }, fileOperationExecutor);
  }

  public CompletableFuture<Boolean> deleteEntryAsync(ClipboardEntry entry) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        LocalDate date = entry.getTimestamp().toLocalDate();
        String filePath = getCsvFilePath(date);

        if (!CsvUtil.isValidCsvFile(filePath)) {
          return false;
        }

        List<String[]> rows = CsvUtil.readCsvFile(filePath);
        List<String[]> updatedRows = new ArrayList<>();

        
        if (!rows.isEmpty()) {
          updatedRows.add(rows.get(0));
        }

        for (int i = 1; i < rows.size(); i++) {
          String[] row = rows.get(i);
          if (row.length > 0 && !row[0].equals(entry.getId())) {
            updatedRows.add(row);
          }
        }

        CsvUtil.writeCsvFile(filePath, updatedRows);
        return true;

      } catch (IOException e) {
        System.err.println("エントリの削除に失敗しました: " + e.getMessage());
        return false;
      }
    }, fileOperationExecutor);
  }

  public CompletableFuture<Boolean> updateEntryAsync(ClipboardEntry entry) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        LocalDate date = entry.getTimestamp().toLocalDate();
        String filePath = getCsvFilePath(date);

        if (!CsvUtil.isValidCsvFile(filePath)) {
          return false;
        }

        List<String[]> rows = CsvUtil.readCsvFile(filePath);
        List<String[]> updatedRows = new ArrayList<>();

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
          return true;
        }

        return false;

      } catch (IOException e) {
        System.err.println("エントリの更新に失敗しました: " + e.getMessage());
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

  public String getDataDirectory() {
    return dataDirectory;
  }

  public void shutdown() {
    if (fileOperationExecutor instanceof java.util.concurrent.ExecutorService) {
      ((java.util.concurrent.ExecutorService) fileOperationExecutor).shutdown();
    }
  }
}
