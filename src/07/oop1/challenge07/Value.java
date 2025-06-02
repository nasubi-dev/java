package oop1.challenge07;

import java.util.Objects;

public final class Value<T> {

  private final T value;

  public Value(T value) {
    this.value = value;
  }

  public T get() {
    return value;
  }

  public boolean isNull() {
    return value == null;
  }

  @Override
  public String toString() {
    return String.format("Value[value: %s]", value);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Value<?> other = (Value<?>) obj;
    return Objects.equals(value, other.value);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }
}
