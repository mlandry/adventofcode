package aoccommon;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collector;
import java.util.stream.IntStream;

public record Point(IntArray coordinates) {

  public static Point of(int... coordinates) {
    return new Point(IntArray.of(coordinates));
  }

  public static Point parse(String csv) {
    return new Point(IntArray.wrap(Arrays.stream(csv.split(",")).mapToInt(Integer::parseInt).toArray()));
  }

  public static Collector<Integer, IntArray.Builder, Point> collect() {
    return Collector.of(
        () -> IntArray.builder(),
        (builder, t) -> builder.add(t),
        (b1, b2) -> {
          IntArray.Builder builder = IntArray.builder();
          b1.build().stream().forEach(builder::add);
          b2.build().stream().forEach(builder::add);
          return builder;
        },
        (builder) -> new Point(builder.build()));

  }

  public static Pair<Point, Point> boundingBox(Collection<Point> points) {
    if (points.isEmpty()) {
      return null;
    }
    Point min = null;
    Point max = null;
    Iterator<Point> iterator = points.iterator();
    while (iterator.hasNext()) {
      Point p = iterator.next();
      if (min == null) {
        min = p;
      } else {
        if (min.dimensions() != p.dimensions()) {
          throw new IllegalArgumentException("mismatched dimension count");
        }
        final Point f = min;
        min = Point.of(IntStream.range(0, p.dimensions()).map(i -> Math.min(p.getN(i), f.getN(i))).toArray());
      }
      if (max == null) {
        max = p;
      } else {
        if (min.dimensions() != p.dimensions()) {
          throw new IllegalArgumentException("mismatched dimension count");
        }
        final Point f = max;
        max = Point.of(IntStream.range(0, p.dimensions()).map(i -> Math.max(p.getN(i), f.getN(i))).toArray());
      }
    }
    return Pair.of(min, max);
  }

  public Point copy() {
    return new Point(coordinates.copy());
  }

  public int x() {
    return getX();
  }

  public int getX() {
    return getN(0);
  }

  public int y() {
    return getY();
  }

  public int getY() {
    return getN(1);
  }

  public int z() {
    return getZ();
  }
  
  public int getZ() {
    return getN(2);
  }

  public int getN(int n) {
    return coordinates.get()[n];
  }

  public int dimensions() {
    return coordinates.get().length;
  }

  public IntStream stream() {
    return coordinates.stream();
  }

  public Point merge(Point other, IntBinaryOperator operator) {
    if (dimensions() != other.dimensions()) {
      throw new IllegalArgumentException("Mismatched number of dimensions for merging");
    }
    IntArray.Builder builder = IntArray.builder();
    for (int i = 0; i < dimensions(); i++) {
      builder.add(operator.applyAsInt(getN(i), other.getN(i)));
    }
    return new Point(builder.build());
  }

  @Override
  public String toString() {
    return coordinates.toString();
  }
}