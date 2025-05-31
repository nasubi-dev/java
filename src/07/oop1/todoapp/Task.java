package oop1.todoapp;

import java.time.LocalDate;

public class Task implements Taskable {
  private String description;
  private LocalDate dueDate;
  private boolean completed;

  public Task(String description, LocalDate dueDate) {
    this.description = description;
    this.dueDate = dueDate;
    this.completed = false; // 初期状態は未完了
  }

  public Task(String description) {
    this(description, null);
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public LocalDate getDueDate() {
    return dueDate;
  }

  @Override
  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  @Override
  public boolean isCompleted() {
    return completed;
  }

  @Override
  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    // 完了状態を表示
    if (completed) {
      sb.append("[完了] ");
    } else {
      sb.append("[未完了] ");
    }

    // タスクの説明を表示
    sb.append(description);

    // 期限日を表示
    if (dueDate != null) {
      sb.append(" (期限: ").append(dueDate.format(DATE_FORMATTER)).append(")");
    } else {
      sb.append(" (期限: 未設定)");
    }

    return sb.toString();
  }
}
