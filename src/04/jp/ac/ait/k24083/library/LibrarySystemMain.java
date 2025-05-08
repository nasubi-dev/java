package jp.ac.ait.k24083.library;

public class LibrarySystemMain {
  public static void main(String[] args) {
    System.out.println("===== 図書館管理システム =====");

    // Library オブジェクトを生成
    Library library = new Library();

    System.out.println("\n----- 蔵書登録テスト -----");
    // 本のオブジェクトを生成して追加
    Book book1 = new Book("9784101010014", "人間失格", "太宰治");
    Book book2 = new Book("9784122018617", "銀河鉄道の夜", "宮沢賢治");
    Book book3 = new Book("9784107717818", "少女終末旅行", "つくみず");
    Book book4 = new Book("9784575236286", "告白", "湊かなえ");
    Book book5 = new Book("9784152089953", "少女", "湊かなえ");
    Book book6 = new Book("9784101171319", "旅のラゴス", "筒井康隆");
    // 蔵書に追加
    library.addBook(book1);
    library.addBook(book2);
    library.addBook(book3);
    library.addBook(book4);
    library.addBook(book5);
    library.addBook(book6);

    // 同じISBNの本を追加しようとした場合（エラーケース）
    System.out.println("\n----- 同じISBNの本を追加（エラーケース） -----");
    Book duplicateBook = new Book("9784101010014", "人間失格（重複）", "太宰治");
    library.addBook(duplicateBook);

    // 全蔵書リストを表示
    library.displayAllBooks();

    System.out.println("\n----- 会員登録テスト -----");
    // 会員のオブジェクトを生成して登録
    LibraryMember member1 = new LibraryMember("M001", "山田太郎");
    LibraryMember member2 = new LibraryMember("M002", "鈴木花子", 3); // 最大3冊まで借りられる
    LibraryMember member3 = new LibraryMember("M003", "佐藤次郎");

    // 会員登録
    library.registerMember(member1);
    library.registerMember(member2);
    library.registerMember(member3);

    // 同じIDの会員を登録しようとした場合（エラーケース）
    System.out.println("\n----- 同じIDの会員を登録（エラーケース） -----");
    LibraryMember duplicateMember = new LibraryMember("M001", "山田太郎（重複）");
    library.registerMember(duplicateMember);

    // 会員リストを表示
    library.displayAllMembersWithBorrowedBooks();

    System.out.println("\n----- 追加の会員管理シナリオ -----");

    // 新規会員の登録（様々な貸出制限パターン）
    System.out.println("\n[1] 様々な貸出制限を持つ会員の登録");
    LibraryMember vipMember = new LibraryMember("M004", "高橋修一", 10); // VIP会員：10冊まで借りられる
    LibraryMember restrictedMember = new LibraryMember("M005", "伊藤美咲", 1); // 制限会員：1冊のみ借りられる

    library.registerMember(vipMember);
    library.registerMember(restrictedMember);
    System.out.println("VIP会員と制限会員を登録しました");

    // 会員情報検索のテスト
    System.out.println("\n[2] 会員情報検索テスト");
    LibraryMember foundMember = library.findMemberById("M004");
    if (foundMember != null) {
      System.out.println("ID:M004の会員が見つかりました -> " + foundMember.getName());
    }

    // 存在しない会員IDで検索
    LibraryMember notFoundMember = library.findMemberById("X999");
    if (notFoundMember == null) {
      System.out.println("ID:X999の会員は見つかりませんでした");
    }

    // 退会処理の詳細テスト
    System.out.println("\n[3] 会員退会処理の詳細テスト");

    // 無効なIDで退会しようとする
    System.out.println("- 無効なIDによる退会テスト:");
    library.unregisterMember("");
    library.unregisterMember(null);

    // 存在しないIDで退会しようとする
    System.out.println("\n- 存在しないIDによる退会テスト:");
    library.unregisterMember("M999");

    // 制限会員が本を借りる
    System.out.println("\n- 制限会員(M005)の貸出・退会テスト:");
    library.lendBookToMember("M005", "9784062748469"); // ゼロの使い魔を借りる

    // 本を借りている制限会員を退会させようとする
    library.unregisterMember("M005");

    // 本を返却
    library.receiveBookFromMember("M005", "9784062748469");

    // 返却後に退会
    System.out.println("\n- 返却後の退会テスト:");
    library.unregisterMember("M005");

    // 退会後に同じIDで新しい会員を登録（可能）
    System.out.println("\n- 退会後の同じIDでの再登録テスト:");
    LibraryMember newMember = new LibraryMember("M005", "新規会員");
    library.registerMember(newMember);

    // 現在の会員状況を表示
    System.out.println("\n[4] 会員管理シナリオ後の会員状況");
    library.displayAllMembersWithBorrowedBooks();

    System.out.println("\n----- 貸出テスト -----");
    // member1に本を貸し出し
    library.lendBookToMember("M001", "9784101010014"); // 人間失格
    library.lendBookToMember("M001", "9784122018617"); // 銀河鉄道の夜

    // member2に本を貸し出し（最大3冊まで）
    library.lendBookToMember("M002", "9784087520125"); // ノルウェイの森
    library.lendBookToMember("M002", "9784575236286"); // 告白
    library.lendBookToMember("M002", "9784152089953"); // 少女

    // 上限を超える貸し出し（エラーケース）
    System.out.println("\n----- 貸出上限超過（エラーケース） -----");
    library.lendBookToMember("M002", "9784062748469"); // ゼロの使い魔

    // 存在しない本のISBNを指定（エラーケース）
    System.out.println("\n----- 存在しない本のISBN（エラーケース） -----");
    library.lendBookToMember("M001", "9999999999999");

    // 存在しない会員IDを指定（エラーケース）
    System.out.println("\n----- 存在しない会員ID（エラーケース） -----");
    library.lendBookToMember("M999", "9784101010014");

    // 貸出中の本を別の会員が借りようとする（エラーケース） -----");
    library.lendBookToMember("M003", "9784101010014"); // 既にmember1が借りている

    // 貸出状況を表示
    library.displayAllMembersWithBorrowedBooks();
    library.displayAvailableBooks();

    System.out.println("\n----- 返却テスト -----");
    // 本を返却
    library.receiveBookFromMember("M001", "9784101010014"); // 人間失格を返却
    library.receiveBookFromMember("M002", "9784087520125"); // ノルウェイの森を返却

    // 借りていない本を返却しようとする（エラーケース）
    System.out.println("\n----- 借りていない本を返却（エラーケース） -----");
    library.receiveBookFromMember("M003", "9784101010014");

    // 返却後の貸出状況を表示
    library.displayAllMembersWithBorrowedBooks();
    library.displayAvailableBooks();

    System.out.println("\n----- 書籍検索テスト -----");
    // キーワードで本を検索
    Book[] searchResult1 = library.searchBook("湊");
    displaySearchResults("著者「湊」の検索結果", searchResult1);

    Book[] searchResult2 = library.searchBook("少女");
    displaySearchResults("タイトル「少女」の検索結果", searchResult2);

    Book[] searchResult3 = library.searchBook("少女");
    displaySearchResults("キーワード「少女」の検索結果", searchResult3);

    System.out.println("\n----- 蔵書削除テスト -----");
    // 本を削除
    library.removeBook("9784101010014"); // 返却されたので削除可能

    // 貸出中の本を削除しようとする（エラーケース）
    System.out.println("\n----- 貸出中の本を削除（エラーケース） -----");
    library.removeBook("9784122018617"); // member1が借りているので削除不可

    System.out.println("\n----- 会員退会テスト -----");
    // 会員を退会
    library.unregisterMember("M003"); // 本を借りていないので退会可能

    // 本を借りている会員を退会させようとする（エラーケース）
    System.out.println("\n----- 本を借りている会員の退会（エラーケース） -----");
    library.unregisterMember("M001"); // 本を借りているので退会不可

    // 最終状態を表示
    System.out.println("\n===== 最終状態 =====");
    library.displayAllBooks();
    library.displayAllMembersWithBorrowedBooks();
    library.displayAvailableBooks();
  }

  // 検索結果を表示するヘルパーメソッド
  private static void displaySearchResults(String title, Book[] results) {
    System.out.println("\n----- " + title + " (" + results.length + "件) -----");
    if (results.length == 0) {
      System.out.println("該当する本はありませんでした。");
    } else {
      for (Book book : results) {
        System.out.println(book.getBookDetails());
        System.out.println("-------------------");
      }
    }
  }
}
