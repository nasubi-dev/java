package Clipper.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ClipboardEntry {
  private String id;
  private String text;
  private LocalDateTime timestamp;
  private String source;
  private boolean isFavorite;

  public ClipboardEntry(String text) {
    this.id = UUID.randomUUID().toString();
    this.text = text;
    this.timestamp = LocalDateTime.now();
    this.source = "Unknown";
    this.isFavorite = false;
  }

  public ClipboardEntry(String id, LocalDateTime timestamp, boolean isFavorite, String text) {
    this.id = id;
    this.text = text;
    this.timestamp = timestamp;
    this.source = "Unknown";
    this.isFavorite = isFavorite;
  }

  
  public String getId() {
    return id;
  }

  public String getText() {
    return text;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public String getSource() {
    return source;
  }

  public boolean isFavorite() {
    return isFavorite;
  }

  
  public void setFavorite(boolean favorite) {
    this.isFavorite = favorite;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getPreviewText() {
    if (text == null)
      return "";

    String[] lines = text.split("\n");
    StringBuilder preview = new StringBuilder();

    int maxLines = 2;
    for (int i = 0; i < Math.min(maxLines, lines.length); i++) {
      if (i > 0)
        preview.append("\n");
      String line = lines[i];
      if (line.length() > 50) {
        preview.append(line.substring(0, 47)).append("...");
      } else {
        preview.append(line);
      }
    }

    if (lines.length > maxLines) {
      preview.append("...");
    }

    return preview.toString();
  }

  public String getFormattedTimestamp() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    return timestamp.format(formatter);
  }

  public String[] toCsvArray() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    return new String[] {
        id,
        timestamp.format(formatter),
        String.valueOf(isFavorite),
        "default", 
        text
    };
  }

  @Override
  public String toString() {
    return String.format("ClipboardEntry{id='%s', timestamp=%s, text='%s'}",
        id, timestamp, getPreviewText());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    ClipboardEntry that = (ClipboardEntry) obj;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
