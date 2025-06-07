package oop1.section08.kadai3;

/**
 * 入力文字列の検証を行うインターフェース。
 */
public interface InputValidator {
  /**
   * 指定された入力文字列を検証します。
   * 
   * @param input 検証する文字列
   * @throws ValidationException 検証に失敗した場合
   */
  void validate(String input) throws ValidationException;
}
