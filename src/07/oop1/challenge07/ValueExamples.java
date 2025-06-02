package oop1.challenge07;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ValueExamples {

  public static void main(String[] args) {
    System.out.println("=== Value<T> と ValueUtils の具体的な使用例 ===\n");
    example_DataValidationAndTransformation();
    System.out.println();
  }

  private static void example_DataValidationAndTransformation() {
    System.out.println("【例: データ検証と変換処理】");
    System.out.println("ユーザーから受け取った文字列データを数値に変換し、計算を行う");

    // ユーザーからの入力データ（文字列）
    List<String> userInputs = Arrays.asList("123", "456", "abc", "789", null, "");

    System.out.println("入力データ: " + userInputs);

    // 各入力をValueオブジェクトに包む
    List<Value<String>> inputValues = userInputs.stream()
        .map(ValueUtils::of)
        .collect(Collectors.toList());

    System.out.println("\n処理結果:");
    for (int i = 0; i < inputValues.size(); i++) {
      Value<String> input = inputValues.get(i);

      // 文字列を整数に安全に変換
      Value<Integer> numberValue = ValueUtils.map(input, ValueExamples::safeParseInt);

      // 数値を2倍にする
      Value<Integer> doubledValue = ValueUtils.map(numberValue, x -> x * 2);

      System.out.println(String.format("  入力[%d]: %s → 数値: %s → 2倍: %s",
          i, input, numberValue, doubledValue));
    }
  }

  // ヘルパーメソッド: 安全な整数パース
  private static Integer safeParseInt(String str) {
    if (str == null || str.trim().isEmpty()) {
      return null;
    }
    try {
      return Integer.parseInt(str.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
