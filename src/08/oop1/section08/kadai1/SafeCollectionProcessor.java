package oop1.section08.kadai1;

import java.util.List;

/**
 * コレクション処理に関するインターフェース
 */
interface SafeCollectionProcessor {
    /**
     * 指定された整数のリストから、正の数のみを合計して返します。
     * - リストがnullまたは空の場合は、0を返します (早期リターン)。
     * - リスト内の各要素をチェックし、正の数のみを合計の対象とします。
     * - リスト内のnull要素は無視します（合計に含めません）。
     * @param numbers 整数のリスト
     * @return 正の数の合計。リストが無効な場合は0。
     */
    int sumPositiveNumbers(List<Integer> numbers);

    /**
     * 指定された文字列のリストから、指定された長さ以上の文字列の数をカウントします。
     * - リストがnullまたは空の場合は、0を返します (早期リターン)。
     * - リスト内の各文字列をチェックし、nullや指定長未満のものはカウントしません。
     * @param texts 文字列のリスト
     * @param minLength カウント対象とする文字列の最小長
     * @return 指定長以上の文字列の数。リストが無効な場合は0。
     */
    int countLongStrings(List<String> texts, int minLength);
}