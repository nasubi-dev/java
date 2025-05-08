package jp.ac.ait.k24083.library;

import java.util.ArrayList;

public class LibraryMain {
  public static void main(String[] args) {
    System.out.println("===== 図書館システムテスト =====");

    // 本のインスタンスを10冊生成
    Book[] books = createBooks();

    // 会員のインスタンスを生成
    LibraryMember member1 = new LibraryMember("M001", "佐藤茄子雄");
    LibraryMember member2 = new LibraryMember("M002", "鈴木茄子子"); // 最大3冊まで貸出可能

    System.out.println("\n----- 初期状態 -----");
    member1.displayMemberInfo();
    member2.displayMemberInfo();

    // シナリオ1: 1冊ずつ借りる
    System.out.println("\n----- シナリオ1: 1冊ずつ借りる -----");
    member1.borrowBook(books[0]);
    member1.borrowBook(books[1]);
    member1.displayMemberInfo();

    // 本の貸出状況確認
    System.out.println("\n本の状態確認:");
    System.out.println(books[0].getBookDetails());

    // シナリオ2: 複数の本をリストで借りる
    System.out.println("\n----- シナリオ2: 複数の本をリストで借りる -----");
    ArrayList<Book> booksToLend = new ArrayList<>();
    booksToLend.add(books[2]);
    booksToLend.add(books[3]);
    booksToLend.add(books[4]);

    int lentCount = member1.borrowBooks(booksToLend);
    System.out.println("貸し出された本の数: " + lentCount + "冊");
    member1.displayMemberInfo();

    // シナリオ3: 貸出上限のチェック
    System.out.println("\n----- シナリオ3: 貸出上限のチェック -----");
    System.out.println("さらに借りられるかチェック: " + member1.canBorrowMore());
    member1.borrowBook(books[5]); // 上限に達しているため失敗するはず

    // シナリオ4: 返却処理
    System.out.println("\n----- シナリオ4: 返却処理 -----");
    member1.returnBook(books[0]);
    member1.displayMemberInfo();

    System.out.println("\n返却後、再度借りられるかチェック: " + member1.canBorrowMore());
    member1.borrowBook(books[5]); // 1冊返却したので借りられるはず
    member1.displayMemberInfo();

    // シナリオ5: 既に貸し出されている本を借りようとする
    System.out.println("\n----- シナリオ5: 既に貸し出されている本を借りようとする -----");
    LibraryMember member3 = new LibraryMember("M003", "山田次郎");
    member3.borrowBook(books[6]); // 既にmember2が借りている

    // シナリオ6: 返却エラーのテスト
    System.out.println("\n----- シナリオ6: 返却エラーのテスト -----");
    member3.returnBook(books[6]); // 借りていない本を返却しようとする

    // 最終状態の確認
    System.out.println("\n===== 最終状態 =====");
    member1.displayMemberInfo();
    member2.displayMemberInfo();
    member3.displayMemberInfo();
  }

  // テスト用の本を生成するメソッド
  private static Book[] createBooks() {
    Book[] books = new Book[10];

    books[0] = new Book("9784101289533", "遮光", "中村文則");
    books[1] = new Book("9784042110019", "ハリー・ポッターと賢者の石", "J.K.ローリング");
    books[2] = new Book("9784101010014", "人間失格", "太宰治");
    books[3] = new Book("9784122018617", "銀河鉄道の夜", "宮沢賢治");
    books[4] = new Book("9784087520125", "ノルウェイの森", "村上春樹");
    books[5] = new Book("9784575236286", "告白", "湊かなえ");
    books[6] = new Book("9784152089953", "少女", "湊かなえ");
    books[7] = new Book("9784062748469", "ゼロの使い魔", "山口升");
    books[8] = new Book("9784101171319", "旅のラゴス", "筒井康隆");
    books[9] = new Book("9784107717818", "少女終末旅行", "つくみず");

    // 一部のフィールド値が不正な本も作成してエラーチェック
    System.out.println("\n----- 不正な値での本の生成テスト -----");
    Book invalidBook1 = new Book("", "タイトルなし", "著者あり");
    Book invalidBook2 = new Book("9784295210307", "", "タイトル無し太郎");
    Book invalidBook3 = new Book("9784295210308", "著者なし", "");

    return books;
  }
}
