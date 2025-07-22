package Clipper.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CsvUtil {

  private static final String CSV_SEPARATOR = ",";
  private static final String CSV_QUOTE = "\"";
  private static final String CSV_ESCAPED_QUOTE = "\"\"";
  private static final Pattern CSV_QUOTE_PATTERN = Pattern.compile("\"");

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
          if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
            currentField.append('"');
            i++;
          } else {
            inQuotes = false;
          }
        } else {
          inQuotes = true;
          quotedField = true;
        }
      } else if (c == ',' && !inQuotes) {
        fields.add(currentField.toString());
        currentField = new StringBuilder();
        quotedField = false;
      } else {
        currentField.append(c);
      }
    }

    fields.add(currentField.toString());

    return fields.toArray(new String[0]);
  }

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

    String escaped = CSV_QUOTE_PATTERN.matcher(field).replaceAll(CSV_ESCAPED_QUOTE);

    return CSV_QUOTE + escaped + CSV_QUOTE;
  }

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

  public static void writeCsvFile(String filePath, List<String[]> rows) throws IOException {
    File file = new File(filePath);
    File parentDir = file.getParentFile();

    
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

  public static void appendCsvLine(String filePath, String[] values) throws IOException {
    File file = new File(filePath);
    File parentDir = file.getParentFile();

    
    if (parentDir != null && !parentDir.exists()) {
      if (!parentDir.mkdirs()) {
        throw new IOException("ディレクトリの作成に失敗しました: " + parentDir.getAbsolutePath());
      }
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
      writeCsvLine(writer, values);
    }
  }

  public static String[] createCsvHeader() {
    return new String[] { "id", "timestamp", "isFavorite", "category", "text" };
  }

  public static boolean isValidCsvFile(String filePath) {
    File file = new File(filePath);
    return file.exists() && file.isFile() && file.length() > 0;
  }
}
