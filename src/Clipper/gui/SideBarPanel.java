package Clipper.gui;

import Clipper.model.ClipboardData;
import Clipper.service.FileManager;
import Clipper.util.DateUtil;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 左サイドバーに表示する日付別ナビゲーションパネル
 */
public class SideBarPanel extends JPanel {

  private final ClipboardData clipboardData;
  private final FileManager fileManager;
  private final DateSelectionListener dateSelectionListener;

  // UIコンポーネント
  private JTree dateTree;
  private DefaultTreeModel treeModel;
  private DefaultMutableTreeNode rootNode;

  // 選択状態
  private LocalDate selectedDate;

  /**
   * 日付選択通知インターフェース
   */
  public interface DateSelectionListener {
    void onDateSelected(LocalDate date);

    void onFavoritesSelected();

    void onAllEntriesSelected();
  }

  public SideBarPanel(ClipboardData clipboardData, FileManager fileManager, DateSelectionListener listener) {
    this.clipboardData = clipboardData;
    this.fileManager = fileManager;
    this.dateSelectionListener = listener;
    this.selectedDate = LocalDate.now();

    initializeUI();
    loadDateTree();
  }

  /**
   * UIコンポーネントを初期化
   */
  private void initializeUI() {
    setLayout(new BorderLayout());
    setBackground(new Color(248, 248, 248));
    setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
    setPreferredSize(new Dimension(200, 0));

    // ヘッダー
    JPanel headerPanel = createHeaderPanel();
    add(headerPanel, BorderLayout.NORTH);

    // 日付ツリー
    createDateTree();
    JScrollPane treeScrollPane = new JScrollPane(dateTree);
    treeScrollPane.setBorder(null);
    treeScrollPane.setBackground(getBackground());
    add(treeScrollPane, BorderLayout.CENTER);
  }

  /**
   * ヘッダーパネルを作成
   */
  private JPanel createHeaderPanel() {
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBackground(getBackground());
    headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JLabel titleLabel = new JLabel("履歴");
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
    headerPanel.add(titleLabel, BorderLayout.CENTER);

    return headerPanel;
  }

  /**
   * 日付ツリーを作成
   */
  private void createDateTree() {
    // ルートノード
    rootNode = new DefaultMutableTreeNode("履歴");
    treeModel = new DefaultTreeModel(rootNode);
    dateTree = new JTree(treeModel);

    // ツリーの設定
    dateTree.setRootVisible(false);
    dateTree.setShowsRootHandles(true);
    dateTree.setRowHeight(24);
    dateTree.setBackground(getBackground());

    // セルレンダラーをカスタマイズ
    dateTree.setCellRenderer(new DateTreeCellRenderer());

    // 選択リスナー
    dateTree.addTreeSelectionListener(e -> {
      DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) dateTree.getLastSelectedPathComponent();
      if (selectedNode != null) {
        handleNodeSelection(selectedNode);
      }
    });

    // マウスリスナー（ダブルクリック対応）
    dateTree.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          TreePath path = dateTree.getPathForLocation(e.getX(), e.getY());
          if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            handleNodeSelection(node);
          }
        }
      }
    });
  }

  /**
   * 日付ツリーを読み込み
   */
  private void loadDateTree() {
    SwingUtilities.invokeLater(() -> {
      rootNode.removeAllChildren();

      // お気に入りノード
      FavoriteTreeNode favoritesNode = new FavoriteTreeNode("お気に入り", clipboardData.getFavoriteEntries().size());
      rootNode.add(favoritesNode);

      // 最近の日付ノード
      List<LocalDate> recentDates = DateUtil.getRecentDates(30);

      for (LocalDate date : recentDates) {
        CompletableFuture<Integer> countFuture;

        if (date.equals(LocalDate.now())) {
          // 今日の場合はメモリ上のデータから件数を取得
          int count = clipboardData.getEntriesByDate(date).size();
          countFuture = CompletableFuture.completedFuture(count);
        } else {
          // 過去の日付の場合はファイルから読み込んで件数を確認
          countFuture = fileManager.loadEntriesAsync(date)
              .thenApply(List::size);
        }

        countFuture.thenAccept(count -> {
          if (count > 0) { // エントリがある日付のみ表示
            SwingUtilities.invokeLater(() -> {
              DateTreeNode dateNode = new DateTreeNode(date, count);
              rootNode.add(dateNode);
              treeModel.nodeStructureChanged(rootNode);

              // 今日の日付を初期選択
              if (date.equals(LocalDate.now())) {
                TreePath path = new TreePath(new Object[] { rootNode, dateNode });
                dateTree.setSelectionPath(path);
                dateTree.expandPath(path.getParentPath());
              }
            });
          }
        });
      }

      treeModel.nodeStructureChanged(rootNode);

      // ツリーを展開
      expandAllNodes();
    });
  }

  /**
   * ノード選択を処理
   */
  private void handleNodeSelection(DefaultMutableTreeNode node) {
    if (node instanceof DateTreeNode) {
      DateTreeNode dateNode = (DateTreeNode) node;
      selectedDate = dateNode.getDate();
      if (dateSelectionListener != null) {
        dateSelectionListener.onDateSelected(selectedDate);
      }
    } else if (node instanceof FavoriteTreeNode) {
      if (dateSelectionListener != null) {
        dateSelectionListener.onFavoritesSelected();
      }
    }
  }

  /**
   * ツリーを全展開
   */
  private void expandAllNodes() {
    for (int i = 0; i < dateTree.getRowCount(); i++) {
      dateTree.expandRow(i);
    }
  }

  /**
   * 表示を更新
   */
  public void refresh() {
    loadDateTree();
  }

  /**
   * 選択された日付を取得
   */
  public LocalDate getSelectedDate() {
    return selectedDate;
  }

  /**
   * 日付ノードクラス
   */
  private static class DateTreeNode extends DefaultMutableTreeNode {
    private final LocalDate date;
    private final int entryCount;

    public DateTreeNode(LocalDate date, int entryCount) {
      super(DateUtil.formatDateForDisplay(date) + " (" + entryCount + ")");
      this.date = date;
      this.entryCount = entryCount;
    }

    public LocalDate getDate() {
      return date;
    }

    public int getEntryCount() {
      return entryCount;
    }
  }

  /**
   * お気に入りノードクラス
   */
  private static class FavoriteTreeNode extends DefaultMutableTreeNode {
    private final int favoriteCount;

    public FavoriteTreeNode(String text, int favoriteCount) {
      super(text + " (" + favoriteCount + ")");
      this.favoriteCount = favoriteCount;
    }

    public int getFavoriteCount() {
      return favoriteCount;
    }
  }

  /**
   * カスタムツリーセルレンダラー
   */
  private class DateTreeCellRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

      Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

      if (value instanceof DateTreeNode) {
        DateTreeNode dateNode = (DateTreeNode) value;

        // 今日の日付を強調
        if (dateNode.getDate().equals(LocalDate.now())) {
          setFont(getFont().deriveFont(Font.BOLD));
        }

        // アイコンを設定
        setIcon(new ColorIcon(Color.BLUE, 8));

      } else if (value instanceof FavoriteTreeNode) {
        // お気に入りアイコン
        setIcon(new ColorIcon(Color.ORANGE, 8));
        setFont(getFont().deriveFont(Font.BOLD));
      }

      // 背景色の設定
      if (selected) {
        setBackgroundSelectionColor(new Color(184, 207, 229));
      } else {
        setBackgroundNonSelectionColor(getBackground());
      }

      return component;
    }
  }

  /**
   * 簡単なカラーアイコン
   */
  private static class ColorIcon implements Icon {
    private final Color color;
    private final int size;

    public ColorIcon(Color color, int size) {
      this.color = color;
      this.size = size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(color);
      g2.fillOval(x, y, size, size);
      g2.dispose();
    }

    @Override
    public int getIconWidth() {
      return size;
    }

    @Override
    public int getIconHeight() {
      return size;
    }
  }
}
