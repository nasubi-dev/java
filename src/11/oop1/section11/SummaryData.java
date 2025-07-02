package oop1.section11;

public class SummaryData {
  private long totalSales;
  private int transactionCount;

  public SummaryData() {
    this.totalSales = 0;
    this.transactionCount = 0;
  }

  public void addTransaction(int price, int quantity) {
    this.totalSales += (long) price * quantity;
    this.transactionCount++;
  }

  public long getTotalSales() {
    return totalSales;
  }

  public int getTransactionCount() {
    return transactionCount;
  }

  public double getAverageTransactionValue() {
    if (transactionCount == 0) {
      return 0.0;
    }
    return (double) totalSales / transactionCount;
  }

  @Override
  public String toString() {
    return String.format("総売上: %,d円, 取引回数: %,d回, 平均単価: %.2f円",
        totalSales, transactionCount, getAverageTransactionValue());
  }
}
