package oop1.section08.kadai3;

public class RangeValidator implements InputValidator {
  private final int min;
  private final int max;

  public RangeValidator(int min, int max) {
    this.min = min;
    this.max = max;
  }

  @Override
  public void validate(String input) throws ValidationException {
    // 空文字列や空白のみの場合も数値として解釈できないとして扱う
    if (input == null || input.trim().isEmpty()) {
      throw new ValidationException("数値として解釈できません。");
    }

    try {
      int value = Integer.parseInt(input.trim());

      // 範囲チェック
      if (value < min || value > max) {
        throw new ValidationException("数値は" + min + "から" + max + "の間である必要があります。");
      }

    } catch (NumberFormatException e) {
      throw new ValidationException("数値として解釈できません。");
    }
  }
}
