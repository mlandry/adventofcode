package aoccommon;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * A lightweight wrapper around int[] for convenient use a field in Java records
 * (which don't use Arrays.hashCode, Arrays.equals, etc). Allows the convenience
 * of records without sacrificing performance of using a List over an array.
 */
public class IntArray {

  private final int[] array;

  private IntArray(int[] array) {
    this.array = array;
  }

  public static IntArray wrap(int[] array) {
    return new IntArray(array);
  }

  public static IntArray of(int... array) {
    return new IntArray(array);
  }

  public static IntArray create(int size) {
    return new IntArray(new int[size]);
  }

  public int[] get() {
    return this.array;
  }

  public IntArray copy() {
    return new IntArray(Arrays.copyOf(array, array.length));
  }

  public IntStream stream() {
    return Arrays.stream(array);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(array);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof IntArray)) {
      return false;
    }
    return Arrays.equals(array, ((IntArray) other).array);
  }

  @Override
  public String toString() {
    return Arrays.toString(array);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final IntStream.Builder streamBuilder = IntStream.builder();

    public Builder add(int t) {
      streamBuilder.add(t);
      return this;
    }

    public IntArray build() {
      return new IntArray(streamBuilder.build().toArray());
    }
  }
}
