package aoccommon;

import java.util.stream.IntStream;

public record Range<T extends Comparable<T>>(T lower, boolean lowerClosed, T upper, boolean upperClosed) {

  public static <T extends Comparable<T>> Range<T> of(T lower, T upper) {
    return new Range<>(lower, true, upper, false);
  }

  public static <T extends Comparable<T>> Range<T> open(T lower, T upper) {
    return new Range<>(lower, false, upper, false);
  }

  public static <T extends Comparable<T>> Range<T> closed(T lower, T upper) {
    return new Range<>(lower, true, upper, true);
  }

  public boolean contains(T value) {
    int lowerCompare = lower.compareTo(value);
    int upperCompare = value.compareTo(upper);
    return (lowerClosed ? lowerCompare <= 0 : lowerCompare < 0) && (upperClosed ? upperCompare <= 0 : upperCompare < 0);
  }

  public boolean contains(Range<T> other) {
    if (!other.lowerClosed() || !other.upperClosed()) {
      throw new IllegalArgumentException("Can't check if range contains open range.");
    }
    return contains(other.lower()) && contains(other.upper());
  }

  public boolean overlaps(Range<T> other) {
    if (!other.lowerClosed() || !other.upperClosed()) {
      throw new IllegalArgumentException("Can't check if range overlaps open range.");
    }
    return contains(other.lower()) || contains(other.upper());
  }

  @Override
  public String toString() {
    return (lowerClosed ? "[" : "(") + lower.toString() + "," + upper.toString() + (upperClosed ? "]" : ")");
  }

  public static IntStream toIntStream(Range<Integer> intRange) {
    int startInclusive = intRange.lowerClosed ? intRange.lower : intRange.lower + 1;
    return intRange.upperClosed ? IntStream.rangeClosed(startInclusive, intRange.upper) : IntStream.range(startInclusive, intRange.upper);
  }
}