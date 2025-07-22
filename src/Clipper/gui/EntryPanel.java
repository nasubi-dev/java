package Clipper.gui;

import Clipper.model.ClipboardEntry;
import Clipper.service.ClipboardMonitor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 個別のクリップボードエントリを表示するパネル
 */
public class EntryPanel extends JPanel {

  private final ClipboardEntry entry;
  private final ClipboardMonitor clipboardMonitor;
  private final EntryActionListener actionListener;

  // UIコンポーネント
  private JLabel timestampLabel;
  private JTextArea textArea;
  private JButton copyButton;
  private JButton deleteButton;
  private JButton favoriteButton;

  // デザイン設定
  private static final Color NORMAL_BACKGROUND = Color.WHITE;
  private static final Color HOVER_BACKGROUND = new Color(245, 245, 245);
  private static final Color FAVORITE_BACKGROUND = new Color(255, 255, 220);
  private static final Color BORDER_COLOR = new Color(220, 220, 220);

  /**
   * エントリアクション通知インターフェース
   */
  public interface EntryActionListener {
    void onEntryDeleted(ClipboardEntry entry);

    void onEntryFavoriteToggled(ClipboardEntry entry);

    void onEntryCopied(ClipboardEntry entry);
  }

  public EntryPanel(ClipboardEntry entry, ClipboardMonitor clipboardMonitor, EntryActionListener actionListener) {
    this.entry = entry;
    this.clipboardMonitor = clipboardMonitor;
    this.actionListener = actionListener;

    initializeUI();
    setupEventHandlers();
  }

  /**
   * UIコンポーネントを初期化
   */
  private void initializeUI() {
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER_COLOR),
        BorderFactory.createEmptyBorder(8, 8, 8, 8)));

    // 背景色を設定
    updateBackground();

    // ヘッダー部分（タイムスタンプとボタン）
    JPanel headerPanel = createHeaderPanel();
    add(headerPanel, BorderLayout.NORTH);

    // テキスト表示部分
    JPanel textPanel = createTextPanel();
    add(textPanel, BorderLayout.CENTER);

    // 最大サイズを設定してレイアウトを制御
    setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
  }

  /**
   * ヘッダーパネルを作成
   */
  private JPanel createHeaderPanel() {
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setOpaque(false);

    // タイムスタンプラベル
    timestampLabel = new JLabel(entry.getFormattedTimestamp());
    timestampLabel.setFont(timestampLabel.getFont().deriveFont(Font.PLAIN, 11f));
    timestampLabel.setForeground(Color.GRAY);
    headerPanel.add(timestampLabel, BorderLayout.WEST);

    // ボタンパネル
    JPanel buttonPanel = createButtonPanel();
    headerPanel.add(buttonPanel, BorderLayout.EAST);

    return headerPanel;
  }

  /**
   * ボタンパネルを作成
   */
  private JPanel createButtonPanel() {
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
    buttonPanel.setOpaque(false);

    // お気に入りボタン
    favoriteButton = new JButton(entry.isFavorite() ? "★" : "☆");
    favoriteButton.setFont(favoriteButton.getFont().deriveFont(14f));
    favoriteButton.setForeground(entry.isFavorite() ? Color.ORANGE : Color.GRAY);
    favoriteButton.setBorderPainted(false);
    favoriteButton.setContentAreaFilled(false);
    favoriteButton.setFocusPainted(false);
    favoriteButton.setPreferredSize(new Dimension(24, 20));
    favoriteButton.setToolTipText("お気に入り切り替え");

    // コピーボタン
    copyButton = new JButton("📋");
    copyButton.setFont(copyButton.getFont().deriveFont(12f));
    copyButton.setBorderPainted(false);
    copyButton.setContentAreaFilled(false);
    copyButton.setFocusPainted(false);
    copyButton.setPreferredSize(new Dimension(24, 20));
    copyButton.setToolTipText("クリップボードにコピー");

    // 削除ボタン
    deleteButton = new JButton("🗑");
    deleteButton.setFont(deleteButton.getFont().deriveFont(12f));
    deleteButton.setBorderPainted(false);
    deleteButton.setContentAreaFilled(false);
    deleteButton.setFocusPainted(false);
    deleteButton.setPreferredSize(new Dimension(24, 20));
    deleteButton.setToolTipText("削除");

    buttonPanel.add(favoriteButton);
    buttonPanel.add(copyButton);
    buttonPanel.add(deleteButton);

    return buttonPanel;
  }

  /**
   * テキストパネルを作成
   */
  private JPanel createTextPanel() {
    JPanel textPanel = new JPanel(new BorderLayout());
    textPanel.setOpaque(false);
    textPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

    // テキストエリア
    textArea = new JTextArea(entry.getPreviewText());
    textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    textArea.setEditable(false);
    textArea.setOpaque(false);
    textArea.setBackground(getBackground());
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);

    // 高さを調整
    textArea.setRows(Math.min(3, entry.getText().split("\n").length));

    textPanel.add(textArea, BorderLayout.CENTER);

    return textPanel;
  }

  /**
   * イベントハンドラーを設定
   */
  private void setupEventHandlers() {
    // お気に入りボタン
    favoriteButton.addActionListener(e -> {
      if (actionListener != null) {
        actionListener.onEntryFavoriteToggled(entry);
        updateFavoriteButton();
        updateBackground();
      }
    });

    // コピーボタン
    copyButton.addActionListener(e -> {
      clipboardMonitor.copyToClipboard(entry.getText());
      if (actionListener != null) {
        actionListener.onEntryCopied(entry);
      }
      showCopyFeedback();
    });

    // 削除ボタン
    deleteButton.addActionListener(e -> {
      int result = JOptionPane.showConfirmDialog(
          this,
          "このエントリを削除しますか？",
          "削除確認",
          JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE);

      if (result == JOptionPane.YES_OPTION && actionListener != null) {
        actionListener.onEntryDeleted(entry);
      }
    });

    // パネル全体のマウスイベント（ホバー効果）
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        if (!entry.isFavorite()) {
          setBackground(HOVER_BACKGROUND);
        }
      }

      @Override
      public void mouseExited(MouseEvent e) {
        updateBackground();
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        // ダブルクリックでコピー
        if (e.getClickCount() == 2) {
          clipboardMonitor.copyToClipboard(entry.getText());
          if (actionListener != null) {
            actionListener.onEntryCopied(entry);
          }
          showCopyFeedback();
        }
      }
    });

    // テキストエリアにもマウスイベントを設定
    textArea.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          clipboardMonitor.copyToClipboard(entry.getText());
          if (actionListener != null) {
            actionListener.onEntryCopied(entry);
          }
          showCopyFeedback();
        }
      }
    });
  }

  /**
   * 背景色を更新
   */
  private void updateBackground() {
    Color backgroundColor = entry.isFavorite() ? FAVORITE_BACKGROUND : NORMAL_BACKGROUND;
    setBackground(backgroundColor);
    if (textArea != null) {
      textArea.setBackground(backgroundColor);
    }
  }

  /**
   * お気に入りボタンを更新
   */
  private void updateFavoriteButton() {
    favoriteButton.setText(entry.isFavorite() ? "★" : "☆");
    favoriteButton.setForeground(entry.isFavorite() ? Color.ORANGE : Color.GRAY);
  }

  /**
   * コピー完了のフィードバックを表示
   */
  private void showCopyFeedback() {
    // ボタンの色を一時的に変更
    Color originalColor = copyButton.getForeground();
    copyButton.setForeground(Color.GREEN);

    Timer timer = new Timer(500, e -> copyButton.setForeground(originalColor));
    timer.setRepeats(false);
    timer.start();
  }

  /**
   * エントリを取得
   */
  public ClipboardEntry getEntry() {
    return entry;
  }

  /**
   * エントリが更新された際の表示更新
   */
  public void refresh() {
    timestampLabel.setText(entry.getFormattedTimestamp());
    textArea.setText(entry.getPreviewText());
    updateFavoriteButton();
    updateBackground();
    revalidate();
    repaint();
  }
}
