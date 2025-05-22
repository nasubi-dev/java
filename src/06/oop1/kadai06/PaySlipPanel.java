package oop1.kadai06;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * 給与明細を表示するためのカスタム{@link JPanel}です。
 * 従業員オブジェクトの情報を受け取り、整形して描画します。
 */
public class PaySlipPanel extends JPanel {
  private Employee currentEmployee;
  private NumberFormat currencyFormatter;

  public PaySlipPanel() {
    setBackground(Color.WHITE);
    this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.JAPAN);
  }

  public void displayPaySlip(Employee emp) {
    this.currentEmployee = emp;
    repaint(); // パネルの再描画を要求
  }

  public void clearPaySlip() {
    this.currentEmployee = null;
    repaint();
  }

  private String formatCurrency(double value) {
    return currencyFormatter.format(value);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g); // 親クラスの描画処理（背景のクリアなど）

    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2d.setFont(new Font("Meiryo UI", Font.PLAIN, 14)); // フォント設定

    int x = 20; // 描画開始位置 X座標
    int y = 30; // 描画開始位置 Y座標
    int lineHeight = 20; // 各行の高さ
    int valueOffset = 200; // 金額表示位置のオフセット

    g2d.setColor(Color.BLACK);

    if (currentEmployee == null) {
      g2d.drawString("従業員情報を入力し、「給与計算実行」ボタンを押してください。", x, y);
      return;
    }

    g2d.setFont(new Font("Meiryo UI", Font.BOLD, 18));
    g2d.drawString("給与明細書", x, y);
    y += lineHeight;
    g2d.setFont(new Font("Meiryo UI", Font.PLAIN, 14));
    g2d.drawLine(x, y - lineHeight / 2, getWidth() - x, y - lineHeight / 2);
    y += lineHeight;

    g2d.drawString("氏名:", x, y);
    g2d.drawString(currentEmployee.getName(), x + valueOffset, y);
    y += lineHeight;

    g2d.drawString("従業員ID:", x, y);
    g2d.drawString(currentEmployee.getEmployeeId(), x + valueOffset, y);
    y += lineHeight;

    g2d.drawString("従業員種別:", x, y);
    g2d.drawString(currentEmployee.getEmployeeTypeName(), x + valueOffset, y);
    y += lineHeight;

    y += lineHeight / 2; // 少しスペース
    g2d.drawString("【支給】", x, y);
    y += lineHeight;

    if (currentEmployee instanceof PartTimeEmployee) {
      PartTimeEmployee pte = (PartTimeEmployee) currentEmployee;
      g2d.drawString("時給:", x, y);
      g2d.drawString(formatCurrency(pte.getHourlyRate()), x + valueOffset, y);
      y += lineHeight;
      g2d.drawString("労働時間:", x, y);
      g2d.drawString(String.format("%.1f 時間", pte.getHoursWorked()), x + valueOffset, y);
      y += lineHeight;
    } else if (currentEmployee instanceof FullTimeEmployee) {
      FullTimeEmployee fte = (FullTimeEmployee) currentEmployee;
      g2d.drawString("基本給:", x, y);
      g2d.drawString(formatCurrency(fte.getBasePay()), x + valueOffset, y);
      y += lineHeight;
      g2d.drawString("残業手当:", x, y);
      g2d.drawString(formatCurrency(fte.calculateOvertimePay()), x + valueOffset, y);
      y += lineHeight;
      g2d.drawString("賞与:", x, y);
      g2d.drawString(formatCurrency(fte.getBonus()), x + valueOffset, y);
      y += lineHeight;
      if (fte instanceof CommuteAllowanceCalculable) { // instanceofは冗長だが例として
        g2d.drawString("交通費:", x, y);
        g2d.drawString(formatCurrency(fte.getCommuteAllowance()), x + valueOffset, y);
        y += lineHeight;
      }
    }

    g2d.setFont(new Font("Meiryo UI", Font.BOLD, 14));
    g2d.drawString("総支給額:", x, y);
    g2d.drawString(formatCurrency(currentEmployee.calculateGrossPay()), x + valueOffset, y);
    y += lineHeight;
    g2d.setFont(new Font("Meiryo UI", Font.PLAIN, 14));

    y += lineHeight / 2;
    g2d.drawString("【控除】", x, y);
    y += lineHeight;

    if (currentEmployee instanceof FullTimeEmployee) {
      FullTimeEmployee fte = (FullTimeEmployee) currentEmployee;
      g2d.drawString("社会保険料:", x, y);
      g2d.drawString(formatCurrency(fte.calculateSocialInsurance()), x + valueOffset, y);
      y += lineHeight;
      g2d.drawString("所得税:", x, y);
      g2d.drawString(formatCurrency(fte.calculateIncomeTax()), x + valueOffset, y);
      y += lineHeight;
    } else if (currentEmployee instanceof PartTimeEmployee) {
      PartTimeEmployee pte = (PartTimeEmployee) currentEmployee;
      g2d.drawString("所得税:", x, y);
      g2d.drawString(formatCurrency(pte.calculateIncomeTax()), x + valueOffset, y);
      y += lineHeight;
    }

    g2d.setFont(new Font("Meiryo UI", Font.BOLD, 14));
    g2d.drawString("控除合計額:", x, y);
    g2d.drawString(formatCurrency(currentEmployee.calculateTotalDeductions()), x + valueOffset, y);
    y += lineHeight;
    g2d.setFont(new Font("Meiryo UI", Font.PLAIN, 14));

    g2d.drawLine(x, y - lineHeight / 2, getWidth() - x, y - lineHeight / 2);

    y += lineHeight / 2;
    g2d.setFont(new Font("Meiryo UI", Font.BOLD, 16));
    g2d.drawString("差引支給額:", x, y);
    g2d.drawString(formatCurrency(currentEmployee.calculateNetPay()), x + valueOffset, y);
  }
}
