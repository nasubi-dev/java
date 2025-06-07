package oop1.section08.kadai1;

import java.util.List;

public class DefaultSafeCollectionProcessor implements SafeCollectionProcessor {

  @Override
  public int sumPositiveNumbers(List<Integer> numbers) {
    // 早期リターン
    if (numbers == null || numbers.isEmpty()) {
      return 0;
    }

    int sum = 0;
    for (Integer number : numbers) {
      if (number != null && number > 0) {
        sum += number;
      }
    }

    return sum;
  }

  @Override
  public int countLongStrings(List<String> texts, int minLength) {
    // 早期リターン
    if (texts == null || texts.isEmpty()) {
      return 0;
    }

    int count = 0;
    for (String text : texts) {
      if (text != null && text.length() >= minLength) {
        count++;
      }
    }

    return count;
  }
}
