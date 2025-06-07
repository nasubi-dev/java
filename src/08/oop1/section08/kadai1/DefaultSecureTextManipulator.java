package oop1.section08.kadai1;

public class DefaultSecureTextManipulator implements SecureTextManipulator {

  @Override
  public String getFirstNCharsAsUpperCase(String text, int n) {
    // 早期リターン
    if (text == null || text.isEmpty() || n <= 0) {
      return "";
    }

    String extractedText;
    if (text.length() <= n) {
      extractedText = text;
    } else {
      extractedText = text.substring(0, n);
    }

    // 抽出した文字列を大文字に変換して返す
    return extractedText.toUpperCase();
  }
}
