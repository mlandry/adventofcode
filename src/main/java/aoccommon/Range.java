package aoccommon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * This class represents a range of comparable items of type T, with each
 * endpoint being
 * inclusive (closed) or exclusive (open).
 */
public record Range<T extends Comparable<T>>(Endpoint<T> lower, Endpoint<T> upper) {

  /**
   * Endpoint is a wrapper around a bound for a Range that may be inclusive
   * (closed) or exclusive (open).
   */
  public record Endpoint<T extends Comparable<T>>(T value, boolean inclusive) {
    public static <T extends Comparable<T>> Endpoint<T> inclusive(T value) {
      return new Endpoint<>(value, true);
    }

    public static <T extends Comparable<T>> Endpoint<T> exclusive(T value) {
      return new Endpoint<>(value, false);
    }
  }

  /**
   * Range [lowerInclusive, upperExclusive) with closed lower endpoint and open
   * upper endpoint.
   */
  public static <T extends Comparable<T>> Range<T> of(T lowerInclusive, T upperExclusive) {
    return new Range<>(Endpoint.inclusive(lowerInclusive), Endpoint.exclusive(upperExclusive));
  }

  /**
   * Range (lower, upper) with open endpoints.
   */
  public static <T extends Comparable<T>> Range<T> open(T lower, T upper) {
    return new Range<>(Endpoint.exclusive(lower), Endpoint.exclusive(upper));
  }

  /**
   * Range [lower, upper] with closed endpoints.
   */
  public static <T extends Comparable<T>> Range<T> closed(T lower, T upper) {
    return new Range<>(Endpoint.inclusive(lower), Endpoint.inclusive(upper));
  }

  /**
   * True if this range contains the value.
   */
  public boolean contains(T value) {
    int lowerCompare = lower.value().compareTo(value);
    int upperCompare = value.compareTo(upper.value());
    return (lower.inclusive() ? lowerCompare <= 0 : lowerCompare < 0)
        && (upper.inclusive() ? upperCompare >= 0 : upperCompare > 0);
  }
}