package oop1.section08.kadai2;

import java.util.List;

public class DefaultStringListProcessor implements StringListProcessor {

  @Override
  public String concatenateAndUppercase(List<String> texts)
      throws InvalidCollectionDataException, EmptyCollectionException, NullItemInCollectionException {
    // リストがnullの場合の例外処理
    if (texts == null) {
      throw new InvalidCollectionDataException("Input list of strings cannot be null.");
    }

    // リストが空の場合の例外処理
    if (texts.isEmpty()) {
      throw new EmptyCollectionException(
          "Input list of strings cannot be empty. At least one element is required for concatenation.");
    }

    // リスト内のnull要素をチェック
    for (int i = 0; i < texts.size(); i++) {
      if (texts.get(i) == null) {
        throw new NullItemInCollectionException(
            "List of strings contains a null item which is not allowed for concatenation at index " + i, i);
      }
    }

    // 文字列を連結し、大文字に変換
    StringBuilder result = new StringBuilder();
    for (String text : texts) {
      result.append(text);
    }

    return result.toString().toUpperCase();
  }
}
