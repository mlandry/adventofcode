package aoccommon;

import java.util.Arrays;

public record Point (int[] coordinates) {

  public static Point of(int... coordinates) {
    return new Point(coordinates);
  }

  public static Point parse(String csv) {
    return new Point(Arrays.stream(csv.split(",")).mapToInt(Integer::parseInt).toArray());
  }

  public int getX() {
    return getN(0);
  }

  public int getY() {
    return getN(1);
  }

  public int getZ() {
    return getN(2);
  }

  public int getN(int n) {
    return coordinates[n];
  }

  @Override
  public String toString() {
    return Arrays.toString(coordinates);
  }
}