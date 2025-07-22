package Clipper.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * クリップボードデータの管理クラス
 * スレッドセーフな実装でマルチスレッドアクセスに対応
 */
public class ClipboardData {
  private final List<ClipboardEntry> entries;
  private final Set<String> duplicateCheckSet;

  public ClipboardData() {
    this.entries = new CopyOnWriteArrayList<>();
    this.duplicateCheckSet = Collections.synchronizedSet(new HashSet<>());
  }

  /**
   * 新しいエントリを追加
   * 重複チェックを行い、同じテキストが既に存在する場合は追加しない
   */
  public synchronized boolean addEntry(String text) {
    if (text == null || text.trim().isEmpty()) {
      return false;
    }

    String cleanedText = text.trim();

    // 重複チェック
    if (duplicateCheckSet.contains(cleanedText)) {
      return false;
    }

    ClipboardEntry entry = new ClipboardEntry(cleanedText);
    entries.add(0, entry); // 最新のものを先頭に追加
    duplicateCheckSet.add(cleanedText);
    return true;
  }

  /**
   * エントリを削除
   */
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

  /**
   * お気に入りフラグを切り替え
   */
  public synchronized boolean toggleFavorite(String id) {
    for (ClipboardEntry entry : entries) {
      if (entry.getId().equals(id)) {
        entry.setFavorite(!entry.isFavorite());
        return true;
      }
    }
    return false;
  }

  /**
   * 指定されたIDのエントリを取得
   */
  public ClipboardEntry getEntry(String id) {
    return entries.stream()
        .filter(entry -> entry.getId().equals(id))
        .findFirst()
        .orElse(null);
  }

  /**
   * すべてのエントリを取得（コピーを返すためスレッドセーフ）
   */
  public List<ClipboardEntry> getAllEntries() {
    return new ArrayList<>(entries);
  }

  /**
   * 指定された日付のエントリを取得
   */
  public List<ClipboardEntry> getEntriesByDate(LocalDate date) {
    return entries.stream()
        .filter(entry -> entry.getTimestamp().toLocalDate().equals(date))
        .collect(Collectors.toList());
  }

  /**
   * お気に入りエントリを取得
   */
  public List<ClipboardEntry> getFavoriteEntries() {
    return entries.stream()
        .filter(ClipboardEntry::isFavorite)
        .collect(Collectors.toList());
  }

  /**
   * テキスト検索
   */
  public List<ClipboardEntry> searchEntries(String query) {
    if (query == null || query.trim().isEmpty()) {
      return getAllEntries();
    }

    String lowerQuery = query.toLowerCase().trim();
    return entries.stream()
        .filter(entry -> entry.getText().toLowerCase().contains(lowerQuery))
        .collect(Collectors.toList());
  }

  /**
   * 日付別にグループ化されたエントリマップを取得
   */
  public Map<LocalDate, List<ClipboardEntry>> getEntriesGroupedByDate() {
    return entries.stream()
        .collect(Collectors.groupingBy(
            entry -> entry.getTimestamp().toLocalDate(),
            LinkedHashMap::new,
            Collectors.toList()));
  }

  /**
   * 指定された日数より古いエントリを削除
   */
  public synchronized int cleanupOldEntries(int daysToKeep) {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
    List<ClipboardEntry> toRemove = entries.stream()
        .filter(entry -> entry.getTimestamp().isBefore(cutoffDate))
        .filter(entry -> !entry.isFavorite()) // お気に入りは保持
        .collect(Collectors.toList());

    for (ClipboardEntry entry : toRemove) {
      entries.remove(entry);
      duplicateCheckSet.remove(entry.getText());
    }

    return toRemove.size();
  }

  /**
   * すべてのエントリをクリア
   */
  public synchronized void clear() {
    entries.clear();
    duplicateCheckSet.clear();
  }

  /**
   * エントリ数を取得
   */
  public int size() {
    return entries.size();
  }

  /**
   * エントリが空かどうか確認
   */
  public boolean isEmpty() {
    return entries.isEmpty();
  }

  /**
   * 外部データからエントリを読み込み（CSV読み込み時に使用）
   */
  public synchronized void loadEntries(List<ClipboardEntry> loadedEntries) {
    clear();
    for (ClipboardEntry entry : loadedEntries) {
      entries.add(entry);
      duplicateCheckSet.add(entry.getText());
    }
    // タイムスタンプでソート（新しいものが先頭）
    entries.sort((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()));
  }
}
