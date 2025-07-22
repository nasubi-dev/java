package Clipper.gui;

import Clipper.model.ClipboardEntry;
import Clipper.service.ClipboardMonitor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EntryPanel extends JPanel {

  private final ClipboardEntry entry;
  private final ClipboardMonitor clipboardMonitor;
  private final EntryActionListener actionListener;

  private JLabel timestampLabel;
  private JTextArea textArea;
  private JButton copyButton;
  private JButton deleteButton;
  private JButton favoriteButton;

  private static final Color NORMAL_BACKGROUND = Color.WHITE;
  private static final Color HOVER_BACKGROUND = new Color(245, 245, 245);
  private static final Color FAVORITE_BACKGROUND = new Color(255, 255, 220);
  private static final Color BORDER_COLOR = new Color(220, 220, 220);

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

  private void initializeUI() {
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER_COLOR),
        BorderFactory.createEmptyBorder(8, 8, 8, 8)));

    updateBackground();

    JPanel headerPanel = createHeaderPanel();
    add(headerPanel, BorderLayout.NORTH);

    JPanel textPanel = createTextPanel();
    add(textPanel, BorderLayout.CENTER);

    setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
  }

  private JPanel createHeaderPanel() {
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setOpaque(false);

    timestampLabel = new JLabel(entry.getFormattedTimestamp());
    timestampLabel.setFont(timestampLabel.getFont().deriveFont(Font.PLAIN, 11f));
    timestampLabel.setForeground(Color.GRAY);
    headerPanel.add(timestampLabel, BorderLayout.WEST);

    JPanel buttonPanel = createButtonPanel();
    headerPanel.add(buttonPanel, BorderLayout.EAST);

    return headerPanel;
  }

  private JPanel createButtonPanel() {
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
    buttonPanel.setOpaque(false);
    buttonPanel.setPreferredSize(new Dimension(150, 36));

    favoriteButton = new JButton(entry.isFavorite() ? "★" : "☆");
    favoriteButton.setFont(favoriteButton.getFont().deriveFont(12f));
    favoriteButton.setForeground(entry.isFavorite() ? Color.ORANGE : Color.GRAY);
    favoriteButton.setBorderPainted(true);
    favoriteButton.setBorder(BorderFactory.createRaisedBevelBorder());
    favoriteButton.setContentAreaFilled(true);
    favoriteButton.setBackground(Color.WHITE);
    favoriteButton.setFocusPainted(false);
    favoriteButton.setMargin(new Insets(2, 4, 2, 4));
    favoriteButton.setPreferredSize(new Dimension(36, 32));
    favoriteButton.setMinimumSize(new Dimension(36, 32));
    favoriteButton.setMaximumSize(new Dimension(36, 32));
    favoriteButton.setHorizontalAlignment(SwingConstants.CENTER);
    favoriteButton.setVerticalAlignment(SwingConstants.CENTER);
    favoriteButton.setToolTipText("お気に入り切り替え");

    copyButton = new JButton("Copy");
    copyButton.setFont(copyButton.getFont().deriveFont(Font.PLAIN, 10f));
    copyButton.setForeground(Color.BLUE);
    copyButton.setBorderPainted(true);
    copyButton.setBorder(BorderFactory.createRaisedBevelBorder());
    copyButton.setContentAreaFilled(true);
    copyButton.setBackground(Color.WHITE);
    copyButton.setFocusPainted(false);
    copyButton.setMargin(new Insets(2, 4, 2, 4));
    copyButton.setPreferredSize(new Dimension(42, 32));
    copyButton.setMinimumSize(new Dimension(42, 32));
    copyButton.setMaximumSize(new Dimension(42, 32));
    copyButton.setHorizontalAlignment(SwingConstants.CENTER);
    copyButton.setVerticalAlignment(SwingConstants.CENTER);
    copyButton.setToolTipText("クリップボードにコピー");

    deleteButton = new JButton("Del");
    deleteButton.setFont(deleteButton.getFont().deriveFont(Font.PLAIN, 10f));
    deleteButton.setForeground(Color.RED);
    deleteButton.setBorderPainted(true);
    deleteButton.setBorder(BorderFactory.createRaisedBevelBorder());
    deleteButton.setContentAreaFilled(true);
    deleteButton.setBackground(Color.WHITE);
    deleteButton.setFocusPainted(false);
    deleteButton.setMargin(new Insets(2, 4, 2, 4));
    deleteButton.setPreferredSize(new Dimension(36, 32));
    deleteButton.setMinimumSize(new Dimension(36, 32));
    deleteButton.setMaximumSize(new Dimension(36, 32));
    deleteButton.setHorizontalAlignment(SwingConstants.CENTER);
    deleteButton.setVerticalAlignment(SwingConstants.CENTER);
    deleteButton.setToolTipText("削除");

    buttonPanel.add(favoriteButton);
    buttonPanel.add(copyButton);
    buttonPanel.add(deleteButton);

    return buttonPanel;
  }

  private JPanel createTextPanel() {
    JPanel textPanel = new JPanel(new BorderLayout());
    textPanel.setOpaque(false);
    textPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

    textArea = new JTextArea(entry.getPreviewText());
    textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    textArea.setEditable(false);
    textArea.setOpaque(false);
    textArea.setBackground(getBackground());
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);

    textArea.setRows(Math.min(3, entry.getText().split("\n").length));

    textPanel.add(textArea, BorderLayout.CENTER);

    return textPanel;
  }

  private void setupEventHandlers() {

    favoriteButton.addActionListener(e -> {
      if (actionListener != null) {
        actionListener.onEntryFavoriteToggled(entry);
        updateFavoriteButton();
        updateBackground();
      }
    });

    copyButton.addActionListener(e -> {
      clipboardMonitor.copyToClipboard(entry.getText());
      if (actionListener != null) {
        actionListener.onEntryCopied(entry);
      }
      showCopyFeedback();
    });

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

        if (e.getClickCount() == 2) {
          clipboardMonitor.copyToClipboard(entry.getText());
          if (actionListener != null) {
            actionListener.onEntryCopied(entry);
          }
          showCopyFeedback();
        }
      }
    });

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

  private void updateBackground() {
    Color backgroundColor = entry.isFavorite() ? FAVORITE_BACKGROUND : NORMAL_BACKGROUND;
    setBackground(backgroundColor);
    if (textArea != null) {
      textArea.setBackground(backgroundColor);
    }
  }

  private void updateFavoriteButton() {
    favoriteButton.setText(entry.isFavorite() ? "★" : "☆");
    favoriteButton.setForeground(entry.isFavorite() ? Color.ORANGE : Color.GRAY);
  }

  private void showCopyFeedback() {
    // ボタンの色変更
    Color originalColor = copyButton.getForeground();
    copyButton.setForeground(Color.GREEN);

    Timer buttonTimer = new Timer(500, e -> copyButton.setForeground(originalColor));
    buttonTimer.setRepeats(false);
    buttonTimer.start();

    // テキストエリアのシークエンシャル表示効果
    showTextSequentially();
  }

  private void showTextSequentially() {
    String fullText = entry.getText();
    int maxLength = Math.min(fullText.length(), 200); // 最大200文字まで表示
    String displayText = fullText.substring(0, maxLength);

    textArea.setText(""); // 一度クリア
    textArea.setBackground(new Color(230, 255, 230)); // 薄い緑の背景
    textArea.setForeground(new Color(0, 100, 0)); // 濃い緑の文字

    // 1秒で全文字を表示するように調整（最低20ms間隔）
    int interval = Math.max(20, 1000 / displayText.length());

    Timer textTimer = new Timer(interval, new ActionListener() {
      private int charIndex = 0;

      @Override
      public void actionPerformed(ActionEvent e) {
        if (charIndex < displayText.length()) {
          textArea.setText(displayText.substring(0, charIndex + 1));

          // カーソル効果を追加
          if (charIndex < displayText.length() - 1) {
            textArea.setText(textArea.getText() + "|");
          }

          charIndex++;

          // スクロールを最下部に
          textArea.setCaretPosition(textArea.getDocument().getLength());
        } else {
          // アニメーション完了
          ((Timer) e.getSource()).stop();
          textArea.setText(displayText); // カーソルを削除

          // 500ms後に元の状態に戻す
          Timer resetTimer = new Timer(500, resetEvent -> {
            updateBackground();
            textArea.setForeground(Color.BLACK); // 元の文字色に戻す
            textArea.setText(entry.getPreviewText());
            ((Timer) resetEvent.getSource()).stop();
          });
          resetTimer.start();
        }
      }
    });
    textTimer.start();
  }

  public ClipboardEntry getEntry() {
    return entry;
  }

  public void refresh() {
    timestampLabel.setText(entry.getFormattedTimestamp());
    textArea.setText(entry.getPreviewText());
    updateFavoriteButton();
    updateBackground();
    revalidate();
    repaint();
  }
}
