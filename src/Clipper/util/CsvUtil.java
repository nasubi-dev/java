package Clipper.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * CSV処理のためのユーティリティクラス
 * CSVの特殊文字（カンマ、改行、ダブルクォート）を適切にエスケープ
 */
public class CsvUtil {

  private static final String CSV_SEPARATOR = ",";
  private static final String CSV_QUOTE = "\"";
  private static final String CSV_ESCAPED_QUOTE = "\"\"";
  private static final Pattern CSV_QUOTE_PATTERN = Pattern.compile("\"");

  /**
   * 文字列配列をCSV行として書き込み
   */
  public static void writeCsvLine(Writer writer, String[] values) throws IOException {
    if (values == null || values.length == 0) {
      writer.write("\n");
      return;
    }

    StringBuilder line = new StringBuilder();
    for (int i = 0; i < values.length; i++) {
      if (i > 0) {
        line.append(CSV_SEPARATOR);
      }
      line.append(escapeCsvField(values[i]));
    }
    line.append("\n");
    writer.write(line.toString());
  }

  /**
   * CSV行を読み取り、文字列配列として返す
   */
  public static String[] parseCsvLine(String line) {
    if (line == null || line.trim().isEmpty()) {
      return new String[0];
    }

    List<String> fields = new ArrayList<>();
    StringBuilder currentField = new StringBuilder();
    boolean inQuotes = false;
    boolean quotedField = false;

    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);

      if (c == '"') {
        if (inQuotes) {
          // クォート内でダブルクォートに遭遇
          if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
            // エスケープされたクォート
            currentField.append('"');
            i++; // 次のクォートをスキップ
          } else {
            // クォート終了
            inQuotes = false;
          }
        } else {
          // クォート開始
          inQuotes = true;
          quotedField = true;
        }
      } else if (c == ',' && !inQuotes) {
        // フィールド区切り
        fields.add(currentField.toString());
        currentField = new StringBuilder();
        quotedField = false;
      } else {
        // 通常の文字
        currentField.append(c);
      }
    }

    // 最後のフィールドを追加
    fields.add(currentField.toString());

    return fields.toArray(new String[0]);
  }

  /**
   * CSVフィールドをエスケープ
   */
  public static String escapeCsvField(String field) {
    if (field == null) {
      return "";
    }

    boolean needsQuoting = field.contains(CSV_SEPARATOR) ||
        field.contains("\n") ||
        field.contains("\r") ||
        field.contains(CSV_QUOTE);

    if (!needsQuoting) {
      return field;
    }

    // ダブルクォートをエスケープ
    String escaped = CSV_QUOTE_PATTERN.matcher(field).replaceAll(CSV_ESCAPED_QUOTE);

    // 全体をダブルクォートで囲む
    return CSV_QUOTE + escaped + CSV_QUOTE;
  }

  /**
   * CSVファイルからすべての行を読み取り
   */
  public static List<String[]> readCsvFile(String filePath) throws IOException {
    List<String[]> rows = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] fields = parseCsvLine(line);
        rows.add(fields);
      }
    }

    return rows;
  }

  /**
   * CSVファイルに行を書き込み
   */
  public static void writeCsvFile(String filePath, List<String[]> rows) throws IOException {
    File file = new File(filePath);
    File parentDir = file.getParentFile();

    // ディレクトリが存在しない場合は作成
    if (parentDir != null && !parentDir.exists()) {
      if (!parentDir.mkdirs()) {
        throw new IOException("ディレクトリの作成に失敗しました: " + parentDir.getAbsolutePath());
      }
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      for (String[] row : rows) {
        writeCsvLine(writer, row);
      }
    }
  }

  /**
   * CSVファイルに行を追記
   */
  public static void appendCsvLine(String filePath, String[] values) throws IOException {
    File file = new File(filePath);
    File parentDir = file.getParentFile();

    // ディレクトリが存在しない場合は作成
    if (parentDir != null && !parentDir.exists()) {
      if (!parentDir.mkdirs()) {
        throw new IOException("ディレクトリの作成に失敗しました: " + parentDir.getAbsolutePath());
      }
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
      writeCsvLine(writer, values);
    }
  }

  /**
   * CSVファイルのヘッダー行を作成
   */
  public static String[] createCsvHeader() {
    return new String[] { "id", "timestamp", "isFavorite", "category", "text" };
  }

  /**
   * ファイルが存在し、空でないかチェック
   */
  public static boolean isValidCsvFile(String filePath) {
    File file = new File(filePath);
    return file.exists() && file.isFile() && file.length() > 0;
  }
}
