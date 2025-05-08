package jp.ac.ait.k24083.library;

import java.util.Arrays;

public class Library {
  // フィールド
  private Book[] books;
  private LibraryMember[] members;

  // コンストラクタ
  public Library() {
    this.books = new Book[0];
    this.members = new LibraryMember[0];
  }

  // 蔵書管理メソッド
  public boolean addBook(Book book) {
    if (book == null) {
      System.out.println("エラー: 追加する本がnullです。");
      return false;
    }

    // 同じISBNの本が既に存在するかチェック
    for (Book existingBook : books) {
      if (existingBook.getIsbn().equals(book.getIsbn())) {
        System.out.println("エラー: ISBN " + book.getIsbn() + " の本は既に登録されています。");
        return false;
      }
    }

    // 本を追加（配列を拡張）
    Book[] newBooks = new Book[books.length + 1];
    System.arraycopy(books, 0, newBooks, 0, books.length);
    newBooks[books.length] = book;
    this.books = newBooks;

    System.out.println("「" + book.getTitle() + "」(ISBN: " + book.getIsbn() + ")を蔵書に追加しました。");
    return true;
  }

  public boolean removeBook(String isbn) {
    if (isbn == null || isbn.isEmpty()) {
      System.out.println("エラー: 無効なISBNです。");
      return false;
    }

    int bookIndex = -1;

    // 本が存在するか、貸出中でないかチェック
    for (int i = 0; i < books.length; i++) {
      if (books[i].getIsbn().equals(isbn)) {
        if (books[i].isBorrowed()) {
          System.out.println("エラー: ISBN " + isbn + " の本は現在貸出中のため削除できません。");
          return false;
        }
        bookIndex = i;
        break;
      }
    }

    if (bookIndex == -1) {
      System.out.println("エラー: ISBN " + isbn + " の本は見つかりません。");
      return false;
    }

    // 本を削除（配列から削除）
    Book removedBook = books[bookIndex];
    Book[] newBooks = new Book[books.length - 1];
    System.arraycopy(books, 0, newBooks, 0, bookIndex);
    System.arraycopy(books, bookIndex + 1, newBooks, bookIndex, books.length - bookIndex - 1);
    this.books = newBooks;

    System.out.println("「" + removedBook.getTitle() + "」(ISBN: " + removedBook.getIsbn() + ")を蔵書から削除しました。");
    return true;
  }

  public Book findBookByIsbn(String isbn) {
    if (isbn == null || isbn.isEmpty()) {
      return null;
    }

    for (Book book : books) {
      if (book.getIsbn().equals(isbn)) {
        return book;
      }
    }

    return null;
  }

  // 会員管理メソッド
  public boolean registerMember(LibraryMember member) {
    if (member == null) {
      System.out.println("エラー: 登録する会員がnullです。");
      return false;
    }

    // 同じIDの会員が既に存在するかチェック
    for (LibraryMember existingMember : members) {
      if (existingMember.getMemberId().equals(member.getMemberId())) {
        System.out.println("エラー: ID " + member.getMemberId() + " の会員は既に登録されています。");
        return false;
      }
    }

    // 会員を追加（配列を拡張）
    LibraryMember[] newMembers = new LibraryMember[members.length + 1];
    System.arraycopy(members, 0, newMembers, 0, members.length);
    newMembers[members.length] = member;
    this.members = newMembers;

    System.out.println(member.getName() + "さん(ID: " + member.getMemberId() + ")を会員として登録しました。");
    return true;
  }

  public boolean unregisterMember(String memberId) {
    if (memberId == null || memberId.isEmpty()) {
      System.out.println("エラー: 無効な会員IDです。");
      return false;
    }

    int memberIndex = -1;

    // 会員が存在するか、本を借りていないかチェック
    for (int i = 0; i < members.length; i++) {
      if (members[i].getMemberId().equals(memberId)) {
        if (members[i].getCurrentBorrowCount() > 0) {
          System.out.println("エラー: ID " + memberId + " の会員は現在本を借りているため退会できません。");
          return false;
        }
        memberIndex = i;
        break;
      }
    }

    if (memberIndex == -1) {
      System.out.println("エラー: ID " + memberId + " の会員は見つかりません。");
      return false;
    }

    // 会員を削除（配列から削除）
    LibraryMember removedMember = members[memberIndex];
    LibraryMember[] newMembers = new LibraryMember[members.length - 1];
    System.arraycopy(members, 0, newMembers, 0, memberIndex);
    System.arraycopy(members, memberIndex + 1, newMembers, memberIndex, members.length - memberIndex - 1);
    this.members = newMembers;

    System.out.println(removedMember.getName() + "さん(ID: " + removedMember.getMemberId() + ")を会員から削除しました。");
    return true;
  }

  public LibraryMember findMemberById(String memberId) {
    if (memberId == null || memberId.isEmpty()) {
      return null;
    }

    for (LibraryMember member : members) {
      if (member.getMemberId().equals(memberId)) {
        return member;
      }
    }

    return null;
  }

  // 貸出・返却業務メソッド
  public boolean lendBookToMember(String memberId, String isbn) {
    LibraryMember member = findMemberById(memberId);
    if (member == null) {
      System.out.println("エラー: ID " + memberId + " の会員は見つかりません。");
      return false;
    }

    Book book = findBookByIsbn(isbn);
    if (book == null) {
      System.out.println("エラー: ISBN " + isbn + " の本は見つかりません。");
      return false;
    }

    return member.borrowBook(book);
  }

  public boolean receiveBookFromMember(String memberId, String isbn) {
    LibraryMember member = findMemberById(memberId);
    if (member == null) {
      System.out.println("エラー: ID " + memberId + " の会員は見つかりません。");
      return false;
    }

    Book book = findBookByIsbn(isbn);
    if (book == null) {
      System.out.println("エラー: ISBN " + isbn + " の本は見つかりません。");
      return false;
    }

    return member.returnBook(book);
  }

  // 書籍検索メソッド
  public Book[] searchBook(String keyword) {
    if (keyword == null || keyword.isEmpty()) {
      return new Book[0];
    }

    keyword = keyword.toLowerCase();
    Book[] result = new Book[0];

    for (Book book : books) {
      if (book.getTitle().toLowerCase().contains(keyword) ||
          book.getAuthor().toLowerCase().contains(keyword)) {
        // 結果配列に追加
        Book[] newResult = new Book[result.length + 1];
        System.arraycopy(result, 0, newResult, 0, result.length);
        newResult[result.length] = book;
        result = newResult;
      }
    }

    return result;
  }

  // 表示系メソッド
  public void displayAllBooks() {
    System.out.println("\n===== 図書館の全蔵書 (" + books.length + "冊) =====");
    if (books.length == 0) {
      System.out.println("蔵書がありません。");
    } else {
      for (int i = 0; i < books.length; i++) {
        System.out.println("\n[蔵書 " + (i + 1) + "]");
        System.out.println(books[i].getBookDetails());
      }
    }
    System.out.println("==========================");
  }

  public void displayAvailableBooks() {
    System.out.println("\n===== 貸出可能な本 =====");
    int availableCount = 0;

    for (int i = 0; i < books.length; i++) {
      if (!books[i].isBorrowed()) {
        System.out.println("\n[蔵書 " + (i + 1) + "]");
        System.out.println(books[i].getBookDetails());
        availableCount++;
      }
    }

    if (availableCount == 0) {
      System.out.println("現在、貸出可能な本はありません。");
    } else {
      System.out.println("\n現在、" + availableCount + "冊の本が貸出可能です。");
    }
    System.out.println("=======================");
  }

  public void displayAllMembersWithBorrowedBooks() {
    System.out.println("\n===== 会員リスト（貸出状況） =====");
    if (members.length == 0) {
      System.out.println("会員が登録されていません。");
    } else {
      for (LibraryMember member : members) {
        member.displayMemberInfo();
      }
    }
    System.out.println("==============================");
  }
}
