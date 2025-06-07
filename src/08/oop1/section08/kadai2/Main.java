package oop1.section08.kadai2;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;

public class Main {
  public static void main(String[] args) {
    NumberListAnalyzer analyzer = new DefaultNumberListAnalyzer();
    System.out.println("--- NumberListAnalyzer ---");

    List<Integer> numbers1 = Arrays.asList(1, 5, 2, 8, 3);
    testFindMaximum(analyzer, "Valid list", numbers1);
    // 期待される出力: Max value = 8

    List<Integer> numbers2 = null;
    testFindMaximum(analyzer, "Null list", numbers2);
    // 期待される出力: Error: Input list cannot be null. (Type:
    // InvalidCollectionDataException)

    List<Integer> numbers3 = Collections.emptyList();
    testFindMaximum(analyzer, "Empty list", numbers3);
    // 期待される出力: Error: Input list cannot be empty... (Type:
    // EmptyCollectionException)

    List<Integer> numbers4 = Arrays.asList(1, 5, null, 8, 3);
    testFindMaximum(analyzer, "List with null item", numbers4);
    // 期待される出力: Error: List contains a null item... at index 2 (Type:
    // NullItemInCollectionException)

    StringListProcessor processor = new DefaultStringListProcessor();
    System.out.println("\n--- StringListProcessor ---");

    List<String> strings1 = Arrays.asList("hello", " ", "world", " ", "java");
    testConcatenate(processor, "Valid strings", strings1);
    // 期待される出力: Result = "HELLO WORLD JAVA"

    List<String> strings2 = null;
    testConcatenate(processor, "Null strings list", strings2);
    // 期待される出力: Error: Input list of strings cannot be null. (Type:
    // InvalidCollectionDataException)

    List<String> strings3 = Collections.emptyList();
    testConcatenate(processor, "Empty strings list", strings3);
    // 期待される出力: Error: Input list of strings cannot be empty... (Type:
    // EmptyCollectionException)

    List<String> strings4 = Arrays.asList("hello", null, "java");
    testConcatenate(processor, "Strings list with null item", strings4);
    // 期待される出力: Error: List of strings contains a null item... at index 1 (Type:
    // NullItemInCollectionException)

    List<String> strings5 = Arrays.asList("one");
    testConcatenate(processor, "Single item list", strings5);
    // 期待される出力: Result = "ONE"
  }

  private static void testFindMaximum(NumberListAnalyzer analyzer, String testCaseName, List<Integer> numbers) {
    System.out.print(testCaseName + ": ");
    try {
      // 例外が発生する可能性のあるメソッド呼び出し
      int max = analyzer.findMaximumValue(numbers);
      System.out.println("Max value = " + max);
    } catch (EmptyCollectionException | NullItemInCollectionException e) {
      // EmptyCollectionException または NullItemInCollectionException をキャッチ
      // これらは InvalidCollectionDataException のサブクラスなので、より具体的に処理できる
      System.err.println("Specific Error: " + e.getMessage() + " (Type: " + e.getClass().getSimpleName() + ")");
    } catch (InvalidCollectionDataException e) {
      // 上記以外の InvalidCollectionDataException (つまり、リストがnullだった場合など直接の
      // InvalidCollectionDataException) をキャッチ
      // または、サブクラスでキャッチされなかった場合のフォールバック
      System.err.println("General Data Error: " + e.getMessage() + " (Type: " + e.getClass().getSimpleName() + ")");
    } catch (Exception e) {
      // 予期しないその他の例外をキャッチ
      System.err.println("Unexpected Error: " + e.getMessage());
      e.printStackTrace(); // スタックトレースを出力してデバッグに役立てる
    }
  }

  private static void testConcatenate(StringListProcessor processor, String testCaseName, List<String> strings) {
    System.out.print(testCaseName + ": ");
    try {
      // 例外が発生する可能性のあるメソッド呼び出し
      String result = processor.concatenateAndUppercase(strings);
      System.out.println("Result = \"" + result + "\"");
    } catch (InvalidCollectionDataException e) { // InvalidCollectionDataExceptionとそのサブクラスをまとめてキャッチ
      // e.getClass().getSimpleName() で実際の例外クラス名を取得できる
      System.err.println("Error: " + e.getMessage() + " (Type: " + e.getClass().getSimpleName() + ")");
    } catch (Exception e) {
      // 予期しないその他の例外
      System.err.println("Unexpected Error: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
