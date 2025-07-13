package oop1.section13;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipPreviewFrame extends JFrame {
  private File zipFile;
  private JTree tree;
  private DefaultMutableTreeNode rootNode;

  public ZipPreviewFrame(File file) throws IOException {
    this.zipFile = file;
    initializeComponents();
    loadZipContents();
    setupWindow();
  }

  private void initializeComponents() {
    rootNode = new DefaultMutableTreeNode(zipFile.getName());
    tree = new JTree(rootNode);
    tree.setRootVisible(true);
    tree.setShowsRootHandles(true);
  }

  private void loadZipContents() throws IOException {
    try (ZipFile zip = new ZipFile(zipFile)) {
      List<ZipEntry> entries = new ArrayList<>();
      Enumeration<? extends ZipEntry> enumeration = zip.entries();
      while (enumeration.hasMoreElements()) {
        entries.add(enumeration.nextElement());
      }

      entries.sort(Comparator.comparing(ZipEntry::getName));

      Map<String, DefaultMutableTreeNode> pathNodeMap = new HashMap<>();
      pathNodeMap.put("", rootNode);

      for (ZipEntry entry : entries) {
        String entryName = entry.getName();
        String[] pathParts = entryName.split("/");

        StringBuilder currentPath = new StringBuilder();
        DefaultMutableTreeNode parentNode = rootNode;

        for (int i = 0; i < pathParts.length; i++) {
          String part = pathParts[i];
          if (part.isEmpty())
            continue;

          if (currentPath.length() > 0) {
            currentPath.append("/");
          }
          currentPath.append(part);

          String fullPath = currentPath.toString();
          DefaultMutableTreeNode currentNode = pathNodeMap.get(fullPath);

          if (currentNode == null) {
            String nodeLabel;
            if (i == pathParts.length - 1 && !entry.isDirectory()) {
              nodeLabel = String.format("%s (%d bytes)", part, entry.getSize());
            } else {
              nodeLabel = part;
            }

            currentNode = new DefaultMutableTreeNode(nodeLabel);
            parentNode.add(currentNode);
            pathNodeMap.put(fullPath, currentNode);
          }

          parentNode = currentNode;
        }
      }

      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      model.reload();

      tree.expandRow(0);

    } catch (IOException e) {
      throw new IOException("ZIPファイルの読み込みに失敗しました: " + e.getMessage(), e);
    }
  }

  private void setupWindow() {
    setTitle("ZIPプレビュー - " + zipFile.getName());
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout());

    JScrollPane scrollPane = new JScrollPane(tree);
    add(scrollPane, BorderLayout.CENTER);

    JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel statusLabel = new JLabel(String.format("ZIPファイルサイズ: %d bytes", zipFile.length()));
    statusPanel.add(statusLabel);
    add(statusPanel, BorderLayout.SOUTH);

    setSize(600, 500);
    setLocationRelativeTo(null);
  }
}
