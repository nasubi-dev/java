package Clipper.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class DateUtil {

  public static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("M月d日");
  public static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

  public static String formatDateForFile(LocalDate date) {
    return date.format(FILE_DATE_FORMATTER);
  }

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

  public static String formatTimestamp(LocalDateTime dateTime) {
    return dateTime.format(TIMESTAMP_FORMATTER);
  }

  public static String formatTime(LocalDateTime dateTime) {
    return dateTime.format(TIME_FORMATTER);
  }

  public static LocalDateTime parseTimestamp(String timestampStr) {
    try {
      return LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
    } catch (DateTimeParseException e) {
      
      return LocalDateTime.now();
    }
  }

  public static LocalDate parseDateFromFile(String dateStr) {
    try {
      return LocalDate.parse(dateStr, FILE_DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      return LocalDate.now();
    }
  }

  public static List<LocalDate> getRecentDates(int days) {
    List<LocalDate> dates = new ArrayList<>();
    LocalDate today = LocalDate.now();

    for (int i = 0; i < days; i++) {
      dates.add(today.minusDays(i));
    }

    return dates;
  }

  public static boolean isToday(LocalDate date) {
    return date.equals(LocalDate.now());
  }

  public static boolean isYesterday(LocalDate date) {
    return date.equals(LocalDate.now().minusDays(1));
  }

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

  public static String generateCsvFileName(LocalDate date) {
    return formatDateForFile(date) + ".csv";
  }
}
