public class ProductItem {
  String productName;
  double unitPrice;
  int quantity;

  // その商品の小計（単価 × 数量）を計算して返すメソッド。
  public double getSubtotal() {
    return unitPrice * quantity;
  }

  // その商品情報をレシートの1行として表示するための整形済み文字列（例: "商品名: りんご, 単価: 100.00, 数量: 3, 小計:
  // 300"）を返すメソッド。
  public String toString() {
    return String.format("商品名: %s, 単価: %.2f, 数量: %d, 小計: %.2f\n", productName, unitPrice, quantity, getSubtotal());
  }
}
