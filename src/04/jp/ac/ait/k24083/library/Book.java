package jp.ac.ait.k24083.library;

public class Book {
  // フィールド
  private final String isbn;
  private String title;
  private String author;
  private boolean isBorrowed = false;

  // コンストラクタ
  public Book(String isbn, String title, String author) {
    // ISBNのチェック
    if (isbn == null || isbn.isEmpty()) {
      this.isbn = "未設定";
      System.err.println("警告: ISBNが設定されていません。");
    } else {
      this.isbn = isbn;
    }

    // タイトルのチェック
    if (title == null || title.isEmpty()) {
      this.title = "不明";
      System.err.println("警告: タイトルが設定されていません。");
    } else {
      this.title = title;
    }

    // 著者のチェック
    if (author == null || author.isEmpty()) {
      this.author = "不明";
      System.err.println("警告: 著者が設定されていません。");
    } else {
      this.author = author;
    }
  }

  // アクセサメソッド
  public String getIsbn() {
    return isbn;
  }

  public String getTitle() {
    return title;
  }

  public String getAuthor() {
    return author;
  }

  public boolean isBorrowed() {
    return isBorrowed;
  }

  // 貸出処理
  public boolean borrowBook() {
    if (isBorrowed) {
      System.out.println("この本は既に貸し出されています。");
      return false;
    } else {
      isBorrowed = true;
      return true;
    }
  }

  // 返却処理
  public boolean returnBook() {
    if (!isBorrowed) {
      System.out.println("この本は既に返却されています。");
      return false;
    } else {
      isBorrowed = false;
      return true;
    }
  }

  // 本の詳細情報を取得
  public String getBookDetails() {
    return "ISBN: " + isbn +
        "\nタイトル: " + title +
        "\n著者: " + author +
        "\n貸出状況: " + (isBorrowed ? "貸出中" : "貸出可能");
  }
}
