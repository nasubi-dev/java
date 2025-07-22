package Clipper;

import Clipper.gui.MainWindow;

import javax.swing.*;

public class ClipperApp {

  public static void main(String[] args) {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Clipper");

    SwingUtilities.invokeLater(() -> {
      try {
        try {
          System.out.println("Look and Feel設定をスキップしました");
        } catch (Exception lafException) {
          System.err.println("Look and Feelの設定に失敗: " + lafException.getMessage());
        }

        MainWindow mainWindow = new MainWindow();
        mainWindow.showWindow();

        System.out.println("Clipperが起動しました。");

      } catch (Exception e) {
        e.printStackTrace();

        JOptionPane.showMessageDialog(null,
            "アプリケーションの起動中にエラーが発生しました:\n" + e.getMessage(),
            "起動エラー",
            JOptionPane.ERROR_MESSAGE);

        System.exit(1);
      }
    });
  }
}
