package Clipper.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 日付関連のユーティリティクラス
 */
public class DateUtil {

  // フォーマット定数
  public static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("M月d日");
  public static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

  /**
   * LocalDateをファイル名用の文字列に変換
   */
  public static String formatDateForFile(LocalDate date) {
    return date.format(FILE_DATE_FORMATTER);
  }

  /**
   * LocalDateを表示用の文字列に変換
   */
  public static String formatDateForDisplay(LocalDate date) {
    LocalDate today = LocalDate.now();
    LocalDate yesterday = today.minusDays(1);

    if (date.equals(today)) {
      return "今日";
    } else if (date.equals(yesterday)) {
      return "昨日";
    } else {
      return date.format(DISPLAY_DATE_FORMATTER);
    }
  }

  /**
   * LocalDateTimeをタイムスタンプ文字列に変換
   */
  public static String formatTimestamp(LocalDateTime dateTime) {
    return dateTime.format(TIMESTAMP_FORMATTER);
  }

  /**
   * LocalDateTimeを時刻文字列に変換
   */
  public static String formatTime(LocalDateTime dateTime) {
    return dateTime.format(TIME_FORMATTER);
  }

  /**
   * タイムスタンプ文字列をLocalDateTimeに変換
   */
  public static LocalDateTime parseTimestamp(String timestampStr) {
    try {
      return LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
    } catch (DateTimeParseException e) {
      // フォールバック：現在時刻を返す
      return LocalDateTime.now();
    }
  }

  /**
   * ファイル名用文字列をLocalDateに変換
   */
  public static LocalDate parseDateFromFile(String dateStr) {
    try {
      return LocalDate.parse(dateStr, FILE_DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      // フォールバック：今日の日付を返す
      return LocalDate.now();
    }
  }

  /**
   * 過去N日間の日付リストを取得
   */
  public static List<LocalDate> getRecentDates(int days) {
    List<LocalDate> dates = new ArrayList<>();
    LocalDate today = LocalDate.now();

    for (int i = 0; i < days; i++) {
      dates.add(today.minusDays(i));
    }

    return dates;
  }

  /**
   * 指定した日付が今日かどうか確認
   */
  public static boolean isToday(LocalDate date) {
    return date.equals(LocalDate.now());
  }

  /**
   * 指定した日付が昨日かどうか確認
   */
  public static boolean isYesterday(LocalDate date) {
    return date.equals(LocalDate.now().minusDays(1));
  }

  /**
   * 相対的な日付表現を取得
   */
  public static String getRelativeDateDescription(LocalDate date) {
    LocalDate today = LocalDate.now();
    long daysDiff = today.toEpochDay() - date.toEpochDay();

    if (daysDiff == 0) {
      return "今日";
    } else if (daysDiff == 1) {
      return "昨日";
    } else if (daysDiff == 2) {
      return "一昨日";
    } else if (daysDiff < 7) {
      return daysDiff + "日前";
    } else if (daysDiff < 14) {
      return "1週間前";
    } else if (daysDiff < 30) {
      return (daysDiff / 7) + "週間前";
    } else {
      return date.format(DISPLAY_DATE_FORMATTER);
    }
  }

  /**
   * CSVファイル名を生成
   */
  public static String generateCsvFileName(LocalDate date) {
    return formatDateForFile(date) + ".csv";
  }
}
