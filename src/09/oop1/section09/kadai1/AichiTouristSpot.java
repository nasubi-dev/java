package oop1.section09.kadai1;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AichiTouristSpot {

  // 愛工大の緯度経度情報
  private static final double AIT_LATITUDE = 35.1834122;
  private static final double AIT_LONGITUDE = 137.1130419;

  // データファイルのパス
  private static final String DATA_DIR = "src/09/oop1/section09/data/";
  private static final String OUTPUT_DIR = "src/09/oop1/section09/output/";
  private static final String OUTPUT_FILE = "TouristSpot.csv";
  // 処理対象のCSVファイル名
  private static final String[] CSV_FILES = {
      "c200326.csv",
      "c200328.csv",
      "c200329.csv",
      "c200330.csv",
      "c200361.csv",
      "c200362.csv",
      "c200363.csv",
      "c200364.csv"
  };

  private static class TouristSpot {
    double latitude;
    double longitude;
    double distance;
    String name;

    public TouristSpot(double latitude, double longitude, String name) {
      this.latitude = latitude;
      this.longitude = longitude;
      this.name = name;
      this.distance = calculateDistance(latitude, longitude);
    }

    private double calculateDistance(double lat, double lon) {
      double deltaLat = lat - AIT_LATITUDE;
      double deltaLon = lon - AIT_LONGITUDE;
      return Math.sqrt(deltaLat * deltaLat + deltaLon * deltaLon);
    }
  }

  public static void main(String[] args) {
    try {
      List<TouristSpot> spots = new ArrayList<>();

      // 各CSVファイルを処理
      for (String fileName : CSV_FILES) {
        List<TouristSpot> fileSpots = processCSVFile(DATA_DIR + fileName);
        spots.addAll(fileSpots);
        System.out.println(fileName + " , 取得データ数: " + fileSpots.size());
      }

      // 距離順でソート
      spots.sort((s1, s2) -> Double.compare(s1.distance, s2.distance));

      // 結果をCSVファイルに出力
      outputToCSV(spots, OUTPUT_DIR + OUTPUT_FILE);

      System.out.println("処理完了。総データ数: " + spots.size());
      System.out.println("出力ファイル: " + OUTPUT_DIR + OUTPUT_FILE);

    } catch (Exception e) {
      System.err.println("エラーが発生しました: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static List<TouristSpot> processCSVFile(String fileName) throws IOException {
    List<TouristSpot> spots = new ArrayList<>();

    try {
      String content = readLineWithEncoding(fileName);
      BufferedReader reader = new BufferedReader(new StringReader(content));

      String line;
      String[] headers = null;
      int lineNumber = 0;

      while ((line = reader.readLine()) != null) {
        lineNumber++;

        if (lineNumber == 1) {
          // ヘッダー行を保存
          headers = parseCSVLine(line);
          continue;
        }

        try {
          String[] values = parseCSVLine(line);
          TouristSpot spot = extractTouristSpot(headers, values, fileName);
          if (spot != null) {
            spots.add(spot);
          }
        } catch (Exception e) {
          System.err.println("行 " + lineNumber + " の処理でエラー: " + e.getMessage());
        }
      }
    } catch (IOException e) {
      System.err.println("ファイル読み込みエラー: " + fileName + " - " + e.getMessage());
      throw e;
    }

    return spots;
  } // 複数のエンコーディングを試行する堅牢なファイル読み込み

  private static String readLineWithEncoding(String fileName) throws IOException {
    String[] encodings = { "Windows-31J", "Shift_JIS", "MS932", "UTF-8" };

    for (String encoding : encodings) {
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(new FileInputStream(fileName), encoding))) {

        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          content.append(line).append("\n");
        }

        return content.toString();
      } catch (Exception e) {
        // このエンコーディングでは読み込めない
        continue;
      }
    }

    // どのエンコーディングでも正常に読み込めない場合はWindows-31Jを使用
    throw new IOException("ファイルを正常に読み込めませんでした: " + fileName);
  }

  private static String[] parseCSVLine(String line) {
    List<String> values = new ArrayList<>();
    boolean inQuotes = false;
    StringBuilder currentValue = new StringBuilder();

    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);

      if (c == '"') {
        if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
          // エスケープされたダブルクォート
          currentValue.append('"');
          i++; // 次の文字をスキップ
        } else {
          inQuotes = !inQuotes;
        }
      } else if (c == ',' && !inQuotes) {
        values.add(currentValue.toString().trim());
        currentValue = new StringBuilder();
      } else {
        currentValue.append(c);
      }
    }

    values.add(currentValue.toString().trim());
    return values.toArray(new String[0]);
  }

  private static TouristSpot extractTouristSpot(String[] headers, String[] values, String fileName) {
    if (headers == null || values == null || headers.length != values.length) {
      return null;
    }

    String coordinateData = null;
    String name = null;

    // 座標データを探す
    for (int i = 0; i < headers.length; i++) {
      String header = headers[i].toLowerCase();
      String value = values[i];

      // 座標情報を含む列を探す
      if (header.contains("形状") || header.contains("point") ||
          (value != null && value.startsWith("POINT("))) {
        coordinateData = value;
        break;
      }
    }

    // ファイル名に基づいて名称列を特定
    name = extractNameByFileName(fileName, values);

    // 座標データから緯度経度を抽出
    if (coordinateData != null && name != null && !name.trim().isEmpty()) {
      double[] coordinates = extractCoordinates(coordinateData);
      if (coordinates != null) {
        return new TouristSpot(coordinates[1], coordinates[0], name); // 緯度、経度の順
      }
    }

    return null;
  }

  private static String extractNameByFileName(String fileName, String[] values) {
    String fileBaseName = fileName.substring(fileName.lastIndexOf('/') + 1);
    String name = null;

    if (fileBaseName.equals("c200326.csv")) {
      // c200326.csv: 4番目の列（index 3）が名称
      name = values.length > 3 ? values[3] : null;
    } else if (fileBaseName.equals("c200328.csv")) {
      // c200328.csv: 3番目の列（index 2）が名称
      name = values.length > 2 ? values[2] : null;
    } else if (fileBaseName.equals("c200329.csv") || fileBaseName.equals("c200330.csv")) {
      // c200329.csv, c200330.csv: 4番目の列（index 3）がデータ名
      name = values.length > 3 ? values[3] : null;
    } else if (fileBaseName.equals("c200361.csv")) {
      // c200361.csv: 5番目の列（index 4）が施設名
      name = values.length > 4 ? values[4] : null;
      // c200361.csvでも文字化けチェックを行う
      if (name != null && isGarbledText(name)) {
        return null;
      }
      return name;
    } else if (fileBaseName.equals("c200362.csv") || fileBaseName.equals("c200363.csv")
        || fileBaseName.equals("c200364.csv")) {
      // c200362.csv, c200363.csv, c200364.csv: 6番目の列（index 5）が名称
      name = values.length > 5 ? values[5] : null;
    }

    // 文字化けした名前を除外
    if (name != null && isGarbledText(name)) {
      return null;
    }

    return name;
  }

  private static boolean isGarbledText(String text) {
    // より具体的な文字化けパターンをチェック
    return text.contains("ï¿½") || text.contains("�") ||
        text.contains("ｿｽ") || text.contains("ﾟ") ||
        text.matches(".*[\\uFFFD].*") ||
        text.matches(".*�ｿｽ.*"); // 特定の文字化けパターン
  }

  private static double[] extractCoordinates(String pointData) {
    Pattern pattern = Pattern.compile("POINT\\(([0-9.]+)\\s+([0-9.]+)\\)");
    Matcher matcher = pattern.matcher(pointData);

    if (matcher.find()) {
      try {
        double longitude = Double.parseDouble(matcher.group(1));
        double latitude = Double.parseDouble(matcher.group(2));
        return new double[] { longitude, latitude };
      } catch (NumberFormatException e) {
        System.err.println("座標データの解析エラー: " + pointData);
      }
    }

    return null;
  }

  private static void outputToCSV(List<TouristSpot> spots, String outputPath) throws IOException {
    File outputFile = new File(outputPath);

    // 既存ファイルがあれば削除
    if (outputFile.exists()) {
      outputFile.delete();
    }

    // 出力ディレクトリが存在しない場合は作成
    outputFile.getParentFile().mkdirs();

    // UTF-8 BOM付きで出力してExcelでも正しく表示されるようにする
    try (FileOutputStream fos = new FileOutputStream(outputFile);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        PrintWriter writer = new PrintWriter(osw)) {

      // UTF-8 BOMを書き込み（Excelでの文字化け防止）
      fos.write(0xEF);
      fos.write(0xBB);
      fos.write(0xBF);

      // ヘッダー行を出力
      writer.println("緯度情報,経度情報,愛工大からの距離,データ名");

      // データ行を出力
      for (TouristSpot spot : spots) {
        writer.printf("%.10f,%.10f,%.10f,%s%n",
            spot.latitude, spot.longitude, spot.distance, spot.name);
      }
    }
  }
}
