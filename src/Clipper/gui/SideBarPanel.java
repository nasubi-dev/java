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

public class SideBarPanel extends JPanel {

  private final ClipboardData clipboardData;
  private final FileManager fileManager;
  private final DateSelectionListener dateSelectionListener;

  // UI Components
  private JTree dateTree;
  private DefaultTreeModel treeModel;
  private DefaultMutableTreeNode rootNode;

  // State
  private LocalDate selectedDate;

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

  private void initializeUI() {
    setLayout(new BorderLayout());
    setBackground(new Color(248, 248, 248));
    setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
    setPreferredSize(new Dimension(200, 0));

    // Header panel
    JPanel headerPanel = createHeaderPanel();
    add(headerPanel, BorderLayout.NORTH);

    // Date tree
    createDateTree();
    JScrollPane treeScrollPane = new JScrollPane(dateTree);
    treeScrollPane.setBorder(null);
    treeScrollPane.setBackground(getBackground());
    add(treeScrollPane, BorderLayout.CENTER);
  }

  private JPanel createHeaderPanel() {
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBackground(getBackground());
    headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JLabel titleLabel = new JLabel("履歴");
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
    headerPanel.add(titleLabel, BorderLayout.CENTER);

    return headerPanel;
  }

  private void createDateTree() {
    // Create tree model
    rootNode = new DefaultMutableTreeNode("履歴");
    treeModel = new DefaultTreeModel(rootNode);
    dateTree = new JTree(treeModel);

    // Configure tree appearance
    dateTree.setRootVisible(false);
    dateTree.setShowsRootHandles(true);
    dateTree.setRowHeight(24);
    dateTree.setBackground(getBackground());

    // Set custom cell renderer
    dateTree.setCellRenderer(new DateTreeCellRenderer());

    // Add selection listener
    dateTree.addTreeSelectionListener(e -> {
      DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) dateTree.getLastSelectedPathComponent();
      if (selectedNode != null) {
        handleNodeSelection(selectedNode);
      }
    });

    // Add double-click listener
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

  private void loadDateTree() {
    SwingUtilities.invokeLater(() -> {
      rootNode.removeAllChildren();

      // お気に入りノード
      int favoriteCount = clipboardData.getFavoriteEntries().size();
      FavoriteTreeNode favoritesNode = new FavoriteTreeNode("お気に入り", favoriteCount);
      rootNode.add(favoritesNode);
      System.out.println("お気に入り数: " + favoriteCount);

      // 単一ファイル対応：メモリ内のデータを日付別にグループ化
      Map<LocalDate, List<Clipper.model.ClipboardEntry>> entriesByDate = clipboardData.getEntriesGroupedByDate();
      System.out.println("総日付数: " + entriesByDate.size());

      // 過去30日の日付を取得して、エントリがある日付のみ表示
      List<LocalDate> recentDates = DateUtil.getRecentDates(30);

      for (LocalDate date : recentDates) {
        List<Clipper.model.ClipboardEntry> entriesForDate = entriesByDate.get(date);
        int count = entriesForDate != null ? entriesForDate.size() : 0;

        if (count > 0) {
          DateTreeNode dateNode = new DateTreeNode(date, count);
          rootNode.add(dateNode);
          System.out.println(date + ": " + count + " エントリ");

          // 今日の日付を自動選択
          if (date.equals(LocalDate.now())) {
            TreePath path = new TreePath(new Object[] { rootNode, dateNode });
            dateTree.setSelectionPath(path);
          }
        }
      }

      treeModel.nodeStructureChanged(rootNode);
      expandTree(); // ツリーを展開
    });
  }

  private void loadDateTreeExcludingFavorites() {
    SwingUtilities.invokeLater(() -> {
      // お気に入りノードを保持して、日付ノードのみ再構築
      FavoriteTreeNode favoritesNode = null;
      
      // 既存のお気に入りノードを探す
      for (int i = 0; i < rootNode.getChildCount(); i++) {
        TreeNode child = rootNode.getChildAt(i);
        if (child instanceof FavoriteTreeNode) {
          favoritesNode = (FavoriteTreeNode) child;
          break;
        }
      }
      
      // 全てのノードを削除
      rootNode.removeAllChildren();
      
      // お気に入りノードを最初に追加（既存のものがあればそれを使用）
      if (favoritesNode != null) {
        rootNode.add(favoritesNode);
      } else {
        // 新規作成（通常は発生しないはず）
        int favoriteCount = clipboardData.getFavoriteEntries().size();
        FavoriteTreeNode newFavoritesNode = new FavoriteTreeNode("お気に入り", favoriteCount);
        rootNode.add(newFavoritesNode);
      }

      // 単一ファイル対応：メモリ内のデータを日付別にグループ化
      Map<LocalDate, List<Clipper.model.ClipboardEntry>> entriesByDate = clipboardData.getEntriesGroupedByDate();
      System.out.println("総日付数: " + entriesByDate.size());

      // 過去30日の日付を取得して、エントリがある日付のみ表示
      List<LocalDate> recentDates = DateUtil.getRecentDates(30);

      for (LocalDate date : recentDates) {
        List<Clipper.model.ClipboardEntry> entriesForDate = entriesByDate.get(date);
        int count = entriesForDate != null ? entriesForDate.size() : 0;

        if (count > 0) {
          DateTreeNode dateNode = new DateTreeNode(date, count);
          rootNode.add(dateNode);
          System.out.println(date + ": " + count + " エントリ");

          // 今日の日付を自動選択
          if (date.equals(LocalDate.now())) {
            TreePath path = new TreePath(new Object[] { rootNode, dateNode });
            dateTree.setSelectionPath(path);
          }
        }
      }

      treeModel.nodeStructureChanged(rootNode);
      expandTree(); // ツリーを展開
    });
  }

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

  public void refresh() {
    // EDTで確実に実行
    SwingUtilities.invokeLater(() -> {
      System.out.println("SideBarPanel全体更新開始");
      
      try {
        loadDateTree();
        
        // 強制的な再描画
        revalidate();
        repaint();
        
        System.out.println("SideBarPanel全体更新完了");
      } catch (Exception e) {
        System.err.println("SideBarPanel更新エラー: " + e.getMessage());
        e.printStackTrace();
      }
    });
  }

  public void refreshDateEntries() {
    // EDTで確実に実行 - お気に入り数は更新せず、日付別エントリのみ更新
    SwingUtilities.invokeLater(() -> {
      System.out.println("SideBarPanel日付別エントリ更新開始");
      
      try {
        loadDateTreeExcludingFavorites();
        
        // 強制的な再描画
        revalidate();
        repaint();
        
        System.out.println("SideBarPanel日付別エントリ更新完了");
      } catch (Exception e) {
        System.err.println("SideBarPanel日付別エントリ更新エラー: " + e.getMessage());
        e.printStackTrace();
      }
    });
  }

  public LocalDate getSelectedDate() {
    return selectedDate;
  }

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

  private class DateTreeCellRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

      Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

      if (value instanceof DateTreeNode) {
        DateTreeNode dateNode = (DateTreeNode) value;

        // 今日の日付は太字で表示
        if (dateNode.getDate().equals(LocalDate.now())) {
          setFont(getFont().deriveFont(Font.BOLD));
        }

        // アイコン設定
        setIcon(new ColorIcon(Color.BLUE, 8));

      } else if (value instanceof FavoriteTreeNode) {
        // お気に入りアイコン
        setIcon(new ColorIcon(Color.ORANGE, 8));
        setFont(getFont().deriveFont(Font.BOLD));
      }

      // 選択色
      if (selected) {
        setBackgroundSelectionColor(new Color(184, 207, 229));
      } else {
        setBackgroundNonSelectionColor(getBackground());
      }

      return component;
    }
  }

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

  private void expandTree() {
    // ルートノードを展開
    dateTree.expandRow(0);
    
    // 最初の数行を展開（お気に入りと最近の日付）
    for (int i = 1; i < Math.min(dateTree.getRowCount(), 8); i++) {
      dateTree.expandRow(i);
    }
  }
}
