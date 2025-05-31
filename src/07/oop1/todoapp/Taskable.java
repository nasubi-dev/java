package oop1.todoapp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public interface Taskable {

  DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

  String getDescription();

  void setDescription(String description);

  LocalDate getDueDate();

  void setDueDate(LocalDate dueDate);

  boolean isCompleted();

  void setCompleted(boolean completed);

  @Override
  String toString();

  static LocalDate parseDueDate(String dateString) {
    if (dateString == null || dateString.trim().isEmpty()) {
      return null;
    }
    try {
      return LocalDate.parse(dateString.trim(), DATE_FORMATTER);
    } catch (DateTimeParseException dtpe) {
      return null;
    }
  }
}
