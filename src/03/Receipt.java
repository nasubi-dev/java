public class Receipt {
  // items : ProductItem[] items; // 商品情報の配列
  ProductItem[] items;

  // コンストラクタ
  public Receipt() {
    // items配列を初期化
    items = new ProductItem[0]; // 空の配列で初期化
  }

  //引数で受け取ったProductItemのオブジェクトをitems配列に追加します。
  public void addProduct(ProductItem item) {
    // items配列のサイズを1つ増やす
    ProductItem[] newItems = new ProductItem[items.length + 1];
    // 既存のitemsを新しい配列にコピー
    System.arraycopy(items, 0, newItems, 0, items.length);
    // 新しい商品を追加
    newItems[items.length] = item;
    // itemsを新しい配列に更新
    items = newItems;
  }

  // itemsに登録されている商品の小計の合計を計算して返すメソッド。
  public double getTotalPrice() {
    double total = 0;
    for (ProductItem item : items) {
      total += item.getSubtotal();
    }
    return total;
  }

  // itemsに登録されている商品の数量の合計（合計商品点数）を計算して返すメソッド。
  public int getTotalQuantity() {
    int total = 0;
    for (ProductItem item : items) {
      total += item.quantity;
    }
    return total;
  }
}
