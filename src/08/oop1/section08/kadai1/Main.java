package oop1.section08.kadai1;

import java.util.List;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {

        SafeCollectionProcessor collectionProcessor = new DefaultSafeCollectionProcessor();
        System.out.println("--- SafeCollectionProcessor ---");

        System.out.println("sumPositiveNumbers:");
        // テストケース1: 通常のリスト (正の数、負の数、null、0を含む)
        System.out.println("Input: [1, -2, 3, null, 0, 5], Expected: 9, Actual: " +
                collectionProcessor.sumPositiveNumbers(Arrays.asList(1, -2, 3, null, 0, 5)));
        // テストケース2: nullリスト (早期リターンの対象)
        System.out.println("Input: null, Expected: 0, Actual: " +
                collectionProcessor.sumPositiveNumbers(null));
        // テストケース3: 空リスト (早期リターンの対象)
        System.out.println("Input: [] (Java 9+), Expected: 0, Actual: " +
                collectionProcessor.sumPositiveNumbers(List.of()));
        // テストケース4: 全て負の数とnullのリスト
        System.out.println("Input: [-1, -5, null], Expected: 0, Actual: " +
                collectionProcessor.sumPositiveNumbers(Arrays.asList(-1, -5, null)));

        System.out.println("\ncountLongStrings (minLength=3):");
        List<String> s1 = Arrays.asList("Java", "is", "fun", null, "Programming", "");

        // テストケース5: 通常の文字列リスト (nullや空文字、条件を満たす/満たさない文字列を含む)
        System.out.println("Input: [\"Java\", \"is\", \"fun\", null, \"Programming\", \"\"], minLength=3, Expected: 3, Actual: " +
                collectionProcessor.countLongStrings(s1, 3));
        // テストケース6: nullリスト (早期リターンの対象)
        System.out.println("Input: null, minLength=3, Expected: 0, Actual: " +
                collectionProcessor.countLongStrings(null, 3));
        // テストケース7: 空リスト (早期リターンの対象)
        System.out.println("Input: [], minLength=3, Expected: 0, Actual: " +
                collectionProcessor.countLongStrings(List.of(), 3));

        List<String> s2 = Arrays.asList("a", "b", "c");

        // テストケース8: 条件を満たす文字列がないリスト
        System.out.println("Input: [\"a\", \"b\", \"c\"], minLength=3, Expected: 0, Actual: " +
                collectionProcessor.countLongStrings(s2, 3));


        SecureTextManipulator textManipulator = new DefaultSecureTextManipulator();
        System.out.println("\n--- SecureTextManipulator ---");
        System.out.println("getFirstNCharsAsUpperCase:");

        // テストケース9: 通常の文字列、nが文字列長より短い
        System.out.println("Input: \"HelloWorld\", n=5, Expected: \"HELLO\", Actual: " + textManipulator.getFirstNCharsAsUpperCase("HelloWorld", 5));
        // テストケース10: 通常の文字列、nが文字列長より長い
        System.out.println("Input: \"Java\", n=10, Expected: \"JAVA\", Actual: " + textManipulator.getFirstNCharsAsUpperCase("Java", 10));
        // テストケース11: 通常の文字列、nが文字列長と等しい
        System.out.println("Input: \"Hi\", n=2, Expected: \"HI\", Actual: " + textManipulator.getFirstNCharsAsUpperCase("Hi", 2));
        // テストケース12: null文字列 (早期リターンの対象)
        System.out.println("Input: null, n=3, Expected: \"\", Actual: " + textManipulator.getFirstNCharsAsUpperCase(null, 3));
        // テストケース13: 空文字列 (早期リターンの対象)
        System.out.println("Input: \"\", n=3, Expected: \"\", Actual: " + textManipulator.getFirstNCharsAsUpperCase("", 3));
        // テストケース14: nが0 (早期リターンの対象)
        System.out.println("Input: \"Test\", n=0, Expected: \"\", Actual: " + textManipulator.getFirstNCharsAsUpperCase("Test", 0));
        // テストケース15: nが負の数 (早期リターンの対象)
        System.out.println("Input: \"Test\", n=-1, Expected: \"\", Actual: " + textManipulator.getFirstNCharsAsUpperCase("Test", -1));
    }
}