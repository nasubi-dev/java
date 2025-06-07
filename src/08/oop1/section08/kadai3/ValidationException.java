package oop1.section08.kadai3;

/**
 * 入力値の検証に失敗した場合にスローされる例外クラス。
 * Exceptionクラスを継承し、エラーメッセージを受け取るコンストラクタを持ちます。
 */
public class ValidationException extends Exception {

  /**
   * エラーメッセージを受け取るコンストラクタ
   * 
   * @param message エラーメッセージ
   */
  public ValidationException(String message) {
    super(message);
  }
}
