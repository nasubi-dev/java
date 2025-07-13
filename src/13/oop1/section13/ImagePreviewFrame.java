package oop1.section13;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public class ImagePreviewFrame extends JFrame {
  private File imageFile;
  private JLabel imageLabel;

  public ImagePreviewFrame(File file) throws IOException {
    this.imageFile = file;
    setLayout(new BorderLayout());
    initializeComponents();
    loadImage();
    setupWindow();
  }

  private void initializeComponents() {
    imageLabel = new JLabel();
    imageLabel.setHorizontalAlignment(JLabel.CENTER);
    imageLabel.setVerticalAlignment(JLabel.CENTER);
  }

  private void loadImage() throws IOException {
    try {
      BufferedImage image = ImageIO.read(imageFile);
      if (image == null) {
        throw new IOException("画像ファイルを読み込めませんでした");
      }

      ImageIcon imageIcon = new ImageIcon(image);
      imageLabel.setIcon(imageIcon);

      JScrollPane scrollPane = new JScrollPane(imageLabel);
      scrollPane.setPreferredSize(new Dimension(800, 600));
      add(scrollPane, BorderLayout.CENTER);

    } catch (IOException e) {
      throw new IOException("画像ファイルの読み込みに失敗しました: " + e.getMessage(), e);
    }
  }

  private void setupWindow() {
    setTitle("画像プレビュー - " + imageFile.getName());
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    pack();

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension windowSize = getSize();

    if (windowSize.width > screenSize.width * 0.8) {
      windowSize.width = (int) (screenSize.width * 0.8);
    }
    if (windowSize.height > screenSize.height * 0.8) {
      windowSize.height = (int) (screenSize.height * 0.8);
    }

    setSize(windowSize);
    setLocationRelativeTo(null);
  }
}
