package Clipper.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ClipboardData {
  private final List<ClipboardEntry> entries;
  private final Set<String> duplicateCheckSet;

  public ClipboardData() {
    this.entries = new CopyOnWriteArrayList<>();
    this.duplicateCheckSet = Collections.synchronizedSet(new HashSet<>());
  }

  public synchronized boolean addEntry(String text) {
    if (text == null || text.trim().isEmpty()) {
      return false;
    }

    String cleanedText = text.trim();

    if (duplicateCheckSet.contains(cleanedText)) {
      return false;
    }

    ClipboardEntry entry = new ClipboardEntry(cleanedText);
    entries.add(0, entry);
    duplicateCheckSet.add(cleanedText);

    System.out.println("エントリ追加成功: 総数=" + entries.size() + ", お気に入り数=" + getFavoriteEntries().size());
    return true;
  }

  public synchronized boolean removeEntry(String id) {
    ClipboardEntry toRemove = null;
    for (ClipboardEntry entry : entries) {
      if (entry.getId().equals(id)) {
        toRemove = entry;
        break;
      }
    }

    if (toRemove != null) {
      entries.remove(toRemove);
      duplicateCheckSet.remove(toRemove.getText());
      return true;
    }
    return false;
  }

  public synchronized boolean toggleFavorite(String id) {
    for (ClipboardEntry entry : entries) {
      if (entry.getId().equals(id)) {
        entry.setFavorite(!entry.isFavorite());
        return true;
      }
    }
    return false;
  }

  public ClipboardEntry getEntry(String id) {
    return entries.stream()
        .filter(entry -> entry.getId().equals(id))
        .findFirst()
        .orElse(null);
  }

  public List<ClipboardEntry> getAllEntries() {
    return new ArrayList<>(entries);
  }

  public List<ClipboardEntry> getEntriesByDate(LocalDate date) {
    return entries.stream()
        .filter(entry -> entry.getTimestamp().toLocalDate().equals(date))
        .collect(Collectors.toList());
  }

  public List<ClipboardEntry> getFavoriteEntries() {
    return entries.stream()
        .filter(ClipboardEntry::isFavorite)
        .collect(Collectors.toList());
  }

  public List<ClipboardEntry> searchEntries(String query) {
    if (query == null || query.trim().isEmpty()) {
      return getAllEntries();
    }

    String lowerQuery = query.toLowerCase().trim();
    return entries.stream()
        .filter(entry -> entry.getText().toLowerCase().contains(lowerQuery))
        .collect(Collectors.toList());
  }

  public Map<LocalDate, List<ClipboardEntry>> getEntriesGroupedByDate() {
    return entries.stream()
        .collect(Collectors.groupingBy(
            entry -> entry.getTimestamp().toLocalDate(),
            LinkedHashMap::new,
            Collectors.toList()));
  }

  public synchronized int cleanupOldEntries(int daysToKeep) {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
    List<ClipboardEntry> toRemove = entries.stream()
        .filter(entry -> entry.getTimestamp().isBefore(cutoffDate))
        .filter(entry -> !entry.isFavorite())
        .collect(Collectors.toList());

    for (ClipboardEntry entry : toRemove) {
      entries.remove(entry);
      duplicateCheckSet.remove(entry.getText());
    }

    return toRemove.size();
  }

  public synchronized void clear() {
    entries.clear();
    duplicateCheckSet.clear();
  }

  public int size() {
    return entries.size();
  }

  public boolean isEmpty() {
    return entries.isEmpty();
  }

  public synchronized void loadEntries(List<ClipboardEntry> loadedEntries) {
    clear();
    for (ClipboardEntry entry : loadedEntries) {
      entries.add(entry);
      duplicateCheckSet.add(entry.getText());
    }

    entries.sort((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()));
  }

  // エントリを復元するメソッド（削除のロールバック用）
  public synchronized boolean restoreEntry(ClipboardEntry entry) {
    if (entry == null) {
      return false;
    }

    // 既に存在する場合は何もしない
    for (ClipboardEntry existing : entries) {
      if (existing.getId().equals(entry.getId())) {
        return false;
      }
    }

    entries.add(entry);
    duplicateCheckSet.add(entry.getText());

    // タイムスタンプ順にソート
    entries.sort((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()));

    return true;
  }
}
