package aoccommon;

public enum Direction {
  UP(0, -1),
  RIGHT(1, 0),
  DOWN(0, 1),
  LEFT(-1, 0);

  private final int xd;
  private final int yd;

  Direction(int xd, int yd) {
    this.xd = xd;
    this.yd = yd;
  }

  public Point apply(Point point) {
    if (point.dimensions() != 2) {
      throw new IllegalArgumentException("Must provide 2-dimensional point");
    }
    return Point.of(point.getX() + xd, point.getY() + yd);
  }

  public Direction opposite() {
    return values()[(ordinal() + 2) % 4];
  }
}
