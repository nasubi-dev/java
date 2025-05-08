package jp.ac.ait.k24083.library;
import java.util.ArrayList;


public class LibraryMember {
  // フィールド
  private final String memberId;
  private String name;
  private Book[] borrowedBooks;
  private int maxBorrowLimit = 5;

  // コンストラクタ（オーバーロード）
  public LibraryMember(String memberId, String name) {
    this.memberId = memberId;
    this.name = name;
    this.borrowedBooks = new Book[maxBorrowLimit];
  }

  public LibraryMember(String memberId, String name, int maxBorrowLimit) {
    this.memberId = memberId;
    this.name = name;

    if (maxBorrowLimit <= 0) {
      this.maxBorrowLimit = 1;
      System.err.println("警告: 最大貸出冊数は1以上である必要があります。");
    } else {
      this.maxBorrowLimit = maxBorrowLimit;
    }

    this.borrowedBooks = new Book[this.maxBorrowLimit];
  }

  // アクセサメソッド
  public String getMemberId() {
    return memberId;
  }

  public String getName() {
    return name;
  }

  public int getMaxBorrowLimit() {
    return maxBorrowLimit;
  }

  // 現在借りている本の冊数を取得
  public int getCurrentBorrowCount() {
    int count = 0;
    for (Book book : borrowedBooks) {
      if (book != null) {
        count++;
      }
    }
    return count;
  }

  // さらに本を借りられるかチェック
  public boolean canBorrowMore() {
    return getCurrentBorrowCount() < maxBorrowLimit;
  }

  // 本を借りる（1冊）
  public boolean borrowBook(Book book) {
    // 貸出上限チェック
    if (!canBorrowMore()) {
      System.out.println("貸出上限に達しています。返却してから借りてください。");
      return false;
    }

    // 本の貸出状態チェック
    if (book.isBorrowed()) {
      System.out.println("この本は既に貸し出されています: " + book.getTitle());
      return false;
    }

    // 本を借りる処理
    if (book.borrowBook()) {
      // 空いている場所を探して本を追加
      for (int i = 0; i < borrowedBooks.length; i++) {
        if (borrowedBooks[i] == null) {
          borrowedBooks[i] = book;
          System.out.println("「" + book.getTitle() + "」を借りました。");
          return true;
        }
      }
    }

    return false;
  }

  // 本を借りる（複数冊）
  public int borrowBooks(ArrayList<Book> booksToBorrow) {
    int successCount = 0;

    for (Book book : booksToBorrow) {
      if (borrowBook(book)) {
        successCount++;
      }
    }

    return successCount;
  }

  // 本を返却する
  public boolean returnBook(Book book) {
    for (int i = 0; i < borrowedBooks.length; i++) {
      if (borrowedBooks[i] != null && borrowedBooks[i].getIsbn().equals(book.getIsbn())) {
        // 本の貸出状態を更新
        if (book.returnBook()) {
          borrowedBooks[i] = null;
          System.out.println("「" + book.getTitle() + "」を返却しました。");
          return true;
        }
      }
    }

    System.out.println("この本はあなたが借りたものではありません: " + book.getTitle());
    return false;
  }

  // 会員情報と貸出状況を表示
  public void displayMemberInfo() {
    System.out.println("=== 会員情報 ===");
    System.out.println("会員ID: " + memberId);
    System.out.println("氏名: " + name);
    System.out.println("最大貸出冊数: " + maxBorrowLimit + "冊");
    System.out.println("現在の貸出冊数: " + getCurrentBorrowCount() + "冊");

    System.out.println("--- 貸出中の本 ---");
    boolean hasBorrowedBooks = false;
    for (Book book : borrowedBooks) {
      if (book != null) {
        System.out.println("・「" + book.getTitle() + "」(ISBN: " + book.getIsbn() + ")");
        hasBorrowedBooks = true;
      }
    }

    if (!hasBorrowedBooks) {
      System.out.println("貸出中の本はありません");
    }
    System.out.println("================");
  }
}
