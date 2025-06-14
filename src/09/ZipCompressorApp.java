

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCompressorApp extends JFrame {
  private JPanel dropPanel;
  private JTextArea fileListArea;
  private JButton compressButton;
  private JLabel statusLabel;
  private List<File> droppedFiles;

  public ZipCompressorApp() {
    droppedFiles = new ArrayList<>();
    initializeGUI();
    setupDragAndDrop();
  }

  /**
   * GUIの初期化
   */
  private void initializeGUI() {
    setTitle("ZIP圧縮アプリケーション");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(600, 500);
    setLocationRelativeTo(null);

    // メインパネル
    JPanel mainPanel = new JPanel(new BorderLayout());

    // ドロップエリア
    dropPanel = new JPanel();
    dropPanel.setBorder(BorderFactory.createTitledBorder("ファイルドロップエリア"));
    dropPanel.setPreferredSize(new Dimension(580, 100));
    dropPanel.setLayout(new BorderLayout());

    JPanel dropContentPanel = new JPanel(new GridLayout(2, 1));
    JLabel dropLabel = new JLabel("ここにファイルやフォルダをドロップしてください", JLabel.CENTER);

    JLabel dropSubLabel = new JLabel("複数ファイル・フォルダの同時ドロップに対応", JLabel.CENTER);

    dropContentPanel.add(dropLabel);
    dropContentPanel.add(dropSubLabel);
    dropPanel.add(dropContentPanel, BorderLayout.CENTER);

    // ファイルリスト表示エリア
    JPanel listPanel = new JPanel(new BorderLayout());
    JLabel listLabel = new JLabel("ドロップされたファイル一覧:");

    fileListArea = new JTextArea(8, 50);
    fileListArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(fileListArea);

    listPanel.add(listLabel, BorderLayout.NORTH);
    listPanel.add(scrollPane, BorderLayout.CENTER);

    // ステータス
    JPanel statusPanel = new JPanel(new BorderLayout());
    statusLabel = new JLabel("");
    statusPanel.add(statusLabel, BorderLayout.NORTH);

    // ボタンパネル
    JPanel buttonPanel = new JPanel(new FlowLayout());

    compressButton = new JButton("ZIP圧縮実行");
    compressButton.setEnabled(false);
    compressButton.addActionListener(e -> compressFiles());

    JButton clearButton = new JButton("リストクリア");
    clearButton.addActionListener(e -> clearFileList());

    JButton exitButton = new JButton("終了");
    exitButton.addActionListener(e -> System.exit(0));

    buttonPanel.add(compressButton);
    buttonPanel.add(clearButton);
    buttonPanel.add(exitButton);

    // 下部パネル
    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.add(statusPanel, BorderLayout.NORTH);
    bottomPanel.add(buttonPanel, BorderLayout.CENTER);

    // レイアウト配置
    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(dropPanel, BorderLayout.NORTH);
    centerPanel.add(listPanel, BorderLayout.CENTER);
    mainPanel.add(centerPanel, BorderLayout.CENTER);

    mainPanel.add(bottomPanel, BorderLayout.SOUTH);

    add(mainPanel);
  }

  private void setupDragAndDrop() {
    new DropTarget(dropPanel, new DropTargetListener() {
      @Override
      public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
          dtde.acceptDrag(DnDConstants.ACTION_COPY);
        } else {
          dtde.rejectDrag();
        }
      }

      @Override
      public void dragOver(DropTargetDragEvent dtde) {
        // ドラッグ中の処理
      }

      @Override
      public void dropActionChanged(DropTargetDragEvent dtde) {
        // アクション変更時の処理
      }

      @Override
      public void dragExit(DropTargetEvent dte) {
        // ドラッグ終了時の処理
      }

      @Override
      public void drop(DropTargetDropEvent dtde) {
        try {
          dtde.acceptDrop(DnDConstants.ACTION_COPY);
          Transferable transferable = dtde.getTransferable();

          if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            @SuppressWarnings("unchecked")
            List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);

            for (File file : files) {
              if (!droppedFiles.contains(file)) {
                droppedFiles.add(file);
              }
            }

            updateFileList();
            dtde.dropComplete(true);
          } else {
            dtde.dropComplete(false);
          }
        } catch (Exception e) {
          showErrorDialog("ファイルのドロップに失敗しました: " + e.getMessage());
          dtde.dropComplete(false);
        }
      }
    });
  }

  private void updateFileList() {
    StringBuilder sb = new StringBuilder();
    for (File file : droppedFiles) {
      sb.append(file.getAbsolutePath()).append("\n");
    }
    fileListArea.setText(sb.toString());
    compressButton.setEnabled(!droppedFiles.isEmpty());

    if (!droppedFiles.isEmpty()) {
      fileListArea.setCaretPosition(fileListArea.getDocument().getLength());
    }
  }

  private void clearFileList() {
    droppedFiles.clear();
    fileListArea.setText("");
    compressButton.setEnabled(false);
  }

  private void compressFiles() {
    if (droppedFiles.isEmpty()) {
      showErrorDialog("圧縮するファイルが選択されていません。");
      return;
    }

    compressButton.setEnabled(false);

    try {
      // 保存先の決定
      File firstFile = droppedFiles.get(0);
      File parentDir = firstFile.getParentFile();
      File zipFile = getUniqueZipFile(parentDir, "archive.zip");

      // ZIP圧縮実行
      compressToZip(droppedFiles, zipFile);

      showSuccessDialog("圧縮完了しました");
    } catch (Exception e) {
      showErrorDialog("圧縮処理中にエラーが発生しました: " + e.getMessage());
    } finally {
      compressButton.setEnabled(true);
    }
  }

  private void compressToZip(List<File> files, File zipFile) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(fos)) {

      for (File file : files) {
        if (file.isDirectory()) {
          addDirectoryToZip(zos, file, file.getName());
        } else {
          addFileToZip(zos, file, file.getName());
        }
      }
    }
  }

  private void addDirectoryToZip(ZipOutputStream zos, File dir, String basePath) throws IOException {
    File[] files = dir.listFiles();
    if (files == null)
      return;

    for (File file : files) {
      String entryPath = basePath + "/" + file.getName();

      if (file.isDirectory()) {
        addDirectoryToZip(zos, file, entryPath);
      } else {
        addFileToZip(zos, file, entryPath);
      }
    }
  }

  private void addFileToZip(ZipOutputStream zos, File file, String entryName) throws IOException {
    ZipEntry zipEntry = new ZipEntry(entryName);
    zos.putNextEntry(zipEntry);

    try (FileInputStream fis = new FileInputStream(file)) {
      byte[] buffer = new byte[8192];
      int length;
      while ((length = fis.read(buffer)) > 0) {
        zos.write(buffer, 0, length);
      }
    }

    zos.closeEntry();
  }

  private File getUniqueZipFile(File parentDir, String baseName) {
    File zipFile = new File(parentDir, baseName);
    int counter = 1;

    while (zipFile.exists()) {
      String name = baseName.substring(0, baseName.lastIndexOf('.'));
      String extension = baseName.substring(baseName.lastIndexOf('.'));
      zipFile = new File(parentDir, name + "_" + counter + extension);
      counter++;
    }

    return zipFile;
  }

  private void showErrorDialog(String message) {
    JOptionPane.showMessageDialog(this, message, "エラー", JOptionPane.ERROR_MESSAGE);
  }

  private void showSuccessDialog(String message) {
    JOptionPane.showMessageDialog(this, message, "圧縮完了", JOptionPane.INFORMATION_MESSAGE);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      new ZipCompressorApp().setVisible(true);
    });
  }
}
