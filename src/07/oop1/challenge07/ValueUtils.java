package oop1.challenge07;

import java.util.function.Function;


public class ValueUtils {

  private ValueUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static <E> Value<E> of(E value) {
    return new Value<>(value);
  }

  public static <T, R> Value<R> map(Value<T> originalValue, Function<T, R> mapper) {
    if (originalValue == null) {
      throw new NullPointerException("originalValue cannot be null");
    }
    if (mapper == null) {
      throw new NullPointerException("mapper cannot be null");
    }

    // 元のValueがnullの値を保持している場合は、mapper関数を適用せずにnullを返す
    if (originalValue.isNull()) {
      return new Value<>(null);
    }

    // mapper関数を適用して新しいValueインスタンスを作成
    R mappedValue = mapper.apply(originalValue.get());
    return new Value<>(mappedValue);
  }
}
