package oop1.challenge07;

public class ValueBusinessExamples {

  public static void main(String[] args) {
    System.out.println("=== 実践的なビジネスロジックでの Value<T> 使用例 ===\n");

    EmailValidationAndFormatting();
  }

  private static void EmailValidationAndFormatting() {
    System.out.println("【実践例: メールアドレスの検証とフォーマット】");
    System.out.println("メールアドレスの検証、正規化");

    String[] emailInputs = {
        "  John.Doe@EXAMPLE.COM  ",
        "invalid-email",
        "user@domain",
        null,
        "ADMIN@company.co.jp",
        "test.user+tag@gmail.com"
    };

    System.out.println("メール処理結果:");

    for (String emailInput : emailInputs) {
      System.out.println("\n入力: \"" + emailInput + "\"");

      // 1. 入力をValueに包む
      Value<String> originalEmail = ValueUtils.of(emailInput);

      // 2. 前後の空白を除去
      Value<String> trimmedEmail = ValueUtils.map(originalEmail, String::trim);

      // 3. 小文字に正規化
      Value<String> normalizedEmail = ValueUtils.map(trimmedEmail, String::toLowerCase);

      // 4. メールアドレスの妥当性を検証
      Value<String> validEmail = ValueUtils.map(normalizedEmail, ValueBusinessExamples::validateEmail);

      // 5. ドメイン部分を抽出
      Value<String> domain = ValueUtils.map(validEmail, ValueBusinessExamples::extractDomain);

      // 結果表示
      System.out.println("  正規化後: " + (normalizedEmail.isNull() ? "処理失敗" : normalizedEmail.get()));
      System.out.println("  検証結果: " + (validEmail.isNull() ? "無効" : "有効"));
      System.out.println("  ドメイン: " + (domain.isNull() ? "抽出失敗" : domain.get()));
    }
  }

  private static String validateEmail(String email) {
    if (email == null || email.isEmpty())
      return null;
    // 簡易的なメール検証（実際の実装ではより厳密な検証が必要）
    if (email.contains("@") && email.contains(".") &&
        email.indexOf("@") > 0 && email.lastIndexOf(".") > email.indexOf("@")) {
      return email;
    }
    return null;
  }

  private static String extractDomain(String email) {
    if (email == null)
      return null;
    int atIndex = email.indexOf("@");
    return atIndex > 0 ? email.substring(atIndex + 1) : null;
  }
}
