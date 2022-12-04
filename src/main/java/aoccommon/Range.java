package aoccommon;

import java.util.Objects;
import java.util.stream.IntStream;

public class Range<T extends Comparable<T>> {
  private final T lower;
  private final boolean lowerClosed;
  private final T upper;
  private final boolean upperClosed;

  protected Range(T lower, boolean lowerClosed, T upper, boolean upperClosed) {
    this.lower = lower;
    this.lowerClosed = lowerClosed;
    this.upper = upper;
    this.upperClosed = upperClosed;
  }

  public static <T extends Comparable<T>> Range<T> of(T lower, T upper) {
    return new Range<>(lower, true, upper, false);
  }

  public static <T extends Comparable<T>> Range<T> open(T lower, T upper) {
    return new Range<>(lower, false, upper, false);
  }

  public static <T extends Comparable<T>> Range<T> closed(T lower, T upper) {
    return new Range<>(lower, true, upper, true);
  }

  public T lower() {
    return lower;
  }

  public boolean lowerClosed() {
    return lowerClosed;
  }

  public T upper() {
    return upper;
  }

  public boolean upperClosed() {
    return upperClosed;
  }

  public boolean contains(T value) {
    int lowerCompare = lower.compareTo(value);
    int upperCompare = value.compareTo(upper);
    return (lowerClosed ? lowerCompare <= 0 : lowerCompare < 0) && (upperClosed ? upperCompare <= 0 : upperCompare < 0);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lower, lowerClosed, upper, upperClosed);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Range)) {
      return false;
    }
    Range otherRange = (Range) other;
    return Objects.equals(lower, otherRange.lower) && lowerClosed == otherRange.lowerClosed
        && Objects.equals(upper, otherRange.upper) && upperClosed == otherRange.upperClosed;
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