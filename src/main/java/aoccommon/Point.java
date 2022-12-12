package aoccommon;

import java.util.Arrays;

public class Point {

  private final int[] coordinates;

  private Point(int[] coordinates) {
    this.coordinates = coordinates;
  }

  public static Point of(int... coordinates) {
    return new Point(coordinates);
  }

  public static Point parse(String csv) {
    return new Point(Arrays.stream(csv.split(",")).mapToInt(Integer::parseInt).toArray());
  }

  public Point copy() {
    return new Point(Arrays.copyOf(coordinates, coordinates.length));
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
  public boolean equals(Object other) {
    if (!(other instanceof Point)) {
      return false;
    }
    return Arrays.equals(coordinates, ((Point) other).coordinates);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(coordinates);
  }

  @Override
  public String toString() {
    return Arrays.toString(coordinates);
  }
}