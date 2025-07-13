package oop1.section13;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainFrame extends JFrame {
  private JButton openDirectoryButton;
  private JList<File> fileList;
  private DefaultListModel<File> listModel;
  private JFrame currentPreviewWindow;
  private File currentDirectory;

  public MainFrame() {
    initializeComponents();
    setupLayout();
    setupEventHandlers();
    setupWindow();
  }

  private void initializeComponents() {
    openDirectoryButton = new JButton("ディレクトリを開く");
    listModel = new DefaultListModel<>();
    fileList = new JList<>(listModel);

    fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    fileList.setCellRenderer(new FileListCellRenderer());
  }

  private void setupLayout() {
    setLayout(new BorderLayout());

    JPanel topPanel = new JPanel(new FlowLayout());
    topPanel.add(openDirectoryButton);
    add(topPanel, BorderLayout.NORTH);

    JScrollPane scrollPane = new JScrollPane(fileList);
    add(scrollPane, BorderLayout.CENTER);
  }

  private void setupEventHandlers() {
    openDirectoryButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        selectDirectory();
      }
    });

    fileList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        File selectedFile = fileList.getSelectedValue();
        if (selectedFile != null && selectedFile.isFile()) {
          openPreviewWindow(selectedFile);
        }
      }
    });
  }

  private void setupWindow() {
    setTitle("ファイルプレビューアプリケーション");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(600, 400);
    setLocationRelativeTo(null);
  }

  private void selectDirectory() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setDialogTitle("ディレクトリを選択");

    if (currentDirectory != null) {
      fileChooser.setCurrentDirectory(currentDirectory);
    }

    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      currentDirectory = fileChooser.getSelectedFile();
      loadDirectoryContents(currentDirectory);
    }
  }

  private void loadDirectoryContents(File directory) {
    listModel.clear();

    if (directory != null && directory.exists() && directory.isDirectory()) {
      File[] files = directory.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.isFile()) { // ファイルのみ表示
            listModel.addElement(file);
          }
        }
      }
    }
  }

  private void openPreviewWindow(File file) {
    // 既存のプレビューウィンドウを閉じる
    if (currentPreviewWindow != null) {
      currentPreviewWindow.dispose();
      currentPreviewWindow = null;
    }

    String fileName = file.getName().toLowerCase();
    String extension = getFileExtension(fileName);

    try {
      if (isImageFile(extension)) {
        currentPreviewWindow = new ImagePreviewFrame(file);
      } else if (isTextFile(extension)) {
        currentPreviewWindow = new TextPreviewFrame(file);
      } else if (isZipFile(extension)) {
        currentPreviewWindow = new ZipPreviewFrame(file);
      } else {
        // 対応していないファイル形式の場合は何もしない
        return;
      }

      if (currentPreviewWindow != null) {
        currentPreviewWindow.setVisible(true);
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this,
          "ファイルが破損しているか、サポートされていない形式です。",
          "エラー",
          JOptionPane.ERROR_MESSAGE);

      if (currentPreviewWindow != null) {
        currentPreviewWindow.dispose();
        currentPreviewWindow = null;
      }
    }
  }

  private String getFileExtension(String fileName) {
    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
      return fileName.substring(lastDotIndex + 1);
    }
    return "";
  }

  private boolean isImageFile(String extension) {
    return extension.matches("png|jpg|jpeg|gif|bmp");
  }

  private boolean isTextFile(String extension) {
    return extension.matches("txt|csv|xml|java|md");
  }

  private boolean isZipFile(String extension) {
    return extension.equals("zip");
  }

  private class FileListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      if (value instanceof File) {
        File file = (File) value;
        setText(file.getName());
        setIcon(FileSystemView.getFileSystemView().getSystemIcon(file));
      }

      return this;
    }
  }
}
