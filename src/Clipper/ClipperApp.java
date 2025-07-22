package Clipper;

import Clipper.gui.MainWindow;

import javax.swing.*;

/**
 * Clipperアプリケーションのメインクラス
 */
public class ClipperApp {

  public static void main(String[] args) {
    // macOS用の設定
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Clipper");

    // EDT（Event Dispatch Thread）でGUIを初期化
    SwingUtilities.invokeLater(() -> {
      try {
        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception lafException) {
          System.err.println("Look and Feelの設定に失敗: " + lafException.getMessage());
        }

        // メインウィンドウを作成・表示
        MainWindow mainWindow = new MainWindow();
        mainWindow.showWindow();

        System.out.println("Clipperが起動しました。");

      } catch (Exception e) {
        e.printStackTrace();

        // エラーダイアログを表示
        JOptionPane.showMessageDialog(null,
            "アプリケーションの起動中にエラーが発生しました:\n" + e.getMessage(),
            "起動エラー",
            JOptionPane.ERROR_MESSAGE);

        System.exit(1);
      }
    });
  }
}
