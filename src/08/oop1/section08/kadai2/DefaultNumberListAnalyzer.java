package oop1.section08.kadai2;

import java.util.List;

public class DefaultNumberListAnalyzer implements NumberListAnalyzer {

  @Override
  public int findMaximumValue(List<Integer> numbers)
      throws InvalidCollectionDataException, EmptyCollectionException, NullItemInCollectionException {
    // リストがnullの場合の例外処理
    if (numbers == null) {
      throw new InvalidCollectionDataException("Input list cannot be null.");
    }

    // リストが空の場合の例外処理
    if (numbers.isEmpty()) {
      throw new EmptyCollectionException(
          "Input list cannot be empty. At least one element is required to find maximum value.");
    }

    // リスト内のnull要素をチェック
    for (int i = 0; i < numbers.size(); i++) {
      if (numbers.get(i) == null) {
        throw new NullItemInCollectionException(
            "List contains a null item which is not allowed for maximum value calculation at index " + i, i);
      }
    }

    // 最大値を計算
    int max = numbers.get(0);
    for (Integer number : numbers) {
      if (number > max) {
        max = number;
      }
    }

    return max;
  }
}
