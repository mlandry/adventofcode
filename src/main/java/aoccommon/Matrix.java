package aoccommon;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Matrix<T>(List<List<T>> rows) {
  public Matrix {
    if (rows == null) {
      throw new IllegalArgumentException("rows cannot be null");
    }
    if (rows.size() > 0) {
      int cols = rows.get(0).size();
      if (!rows.stream().mapToInt(List::size).allMatch(s -> s == cols)) {
        throw new IllegalArgumentException("rows must be same size");
      }
    }
  }

  public int height() {
    return rows.size();
  }

  public int width() {
    return rows.size() == 0 ? 0 : rows.get(0).size();
  }

  public List<List<T>> cols() {
    return IntStream.range(0, width())
        .mapToObj(c -> col(c).toList())
        .toList();
  }

  public T get(int row, int col) {
    return rows.get(row).get(col);
  }

  public T get(Point point) {
    return rows.get(point.getY()).get(point.getX());
  }

  public void set(int row, int col, T value) {
    rows.get(row).set(col, value);
  }

  public void set(Point point, T value) {
    rows.get(point.getY()).set(point.getX(), value);
  }

  public Stream<T> row(int row) {
    return rows.get(row).stream();
  }

  public Stream<T> col(int col) {
    return rows.stream().map(row -> row.get(col));
  }

  public Matrix<T> copy() {
    return new Matrix<>(rows.stream().map(List::stream).map(s -> s.collect(Collectors.toList())).toList());
  }
}
