package oop1.section08.kadai1;

/**
 * 文字列操作に関するインターフェース
 */
public interface SecureTextManipulator {
    /**
     * 指定された文字列の最初のN文字を抽出し、大文字に変換して返します。
     * - 文字列がnullまたは空の場合、あるいはNが0以下の場合は、空文字列 "" を返します (早期リターン)。
     * - 文字列の長さがNより短い場合は、文字列全体を大文字にして返します。
     * @param text 対象の文字列
     * @param n 抽出する文字数
     * @return 処理後の文字列。入力が無効な場合は空文字列。
     */
    String getFirstNCharsAsUpperCase(String text, int n);
}