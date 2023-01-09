package aoccommon;

import java.util.Arrays;

public final record Point(IntArray coordinates) {

  public static Point of(int... coordinates) {
    return new Point(IntArray.of(coordinates));
  }

  public static Point parse(String csv) {
    return new Point(IntArray.wrap(Arrays.stream(csv.split(",")).mapToInt(Integer::parseInt).toArray()));
  }

  public Point copy() {
    return new Point(coordinates.copy());
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
    return coordinates.get()[n];
  }

  @Override
  public String toString() {
    return coordinates.toString();
  }
}