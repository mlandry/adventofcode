import { readLines } from "../utils/utils";

type Point = {
  x: number;
  y: number;
}

function parsePoint(line: string): Point {
  const [x, y] = line.split(',').map(s => Number.parseInt(s));
  return { x, y };
}

function rectangleArea(p1: Point, p2: Point): number {
  return (Math.abs(p1.x - p2.x) + 1) * (Math.abs(p1.y - p2.y) + 1);
}

const points = readLines(import.meta.url, "input.txt").map(parsePoint);

const areas:{area: number, p1: Point, p2: Point}[] = [];
for (let i = 0; i < points.length; i++) {
  const p1 = points[i];
  for (let j = i + 1; j < points.length; j++) {
    const p2 = points[j];
    const area = rectangleArea(p1, p2);
    areas.push({ area, p1, p2 });
  }
}
areas.sort((a, b) => b.area - a.area);
console.log("Part 1: " + areas[0].area);

function* pointsBetween(p1: Point, p2: Point): Generator<Point> {
  if (p1.x === p2.x) {
    for (let y = Math.min(p1.y, p2.y) + 1; y < Math.max(p1.y, p2.y); y++) {
      yield { x: p1.x, y };
    }
  } else {
    for (let x = Math.min(p1.x, p2.x) + 1; x < Math.max(p1.x, p2.x); x++) {
      yield { x, y: p1.y };
    }
  }
}

// x -> set of y values -> "edge" | "corner"
const horizontalLines = new Map<number, Map<number, "edge" | "corner">>();
// y -> set of x values -> "edge" | "corner"
const verticalLines = new Map<number, Map<number, "edge" | "corner">>();

for (let i = 0; i < points.length; i++) {
  const p = points[i];

  const ys = horizontalLines.get(p.x) || new Map<number, "edge" | "corner">();
  ys.set(p.y, "corner");
  horizontalLines.set(p.x, ys);
  const xs = verticalLines.get(p.y) || new Map<number, "edge" | "corner">();
  xs.set(p.x, "corner");
  verticalLines.set(p.y, xs);

  const next = i === points.length - 1 ? points[0] : points[i + 1];
  if (p.y === next.y) {
    for (let x = Math.min(p.x, next.x) + 1; x < Math.max(p.x, next.x); x++) {
      const ys = horizontalLines.get(x) || new Map<number, "edge" | "corner">();
      if (!ys.has(p.y) || ys.get(p.y) !== "corner") {
        ys.set(p.y, "edge");
      }
      horizontalLines.set(x, ys);
    }
  } else {
    for (let y = Math.min(p.y, next.y) + 1; y < Math.max(p.y, next.y); y++) {
      const xs = verticalLines.get(y) || new Map<number, "edge" | "corner">();
      if (!xs.has(p.x) || xs.get(p.x) !== "corner") {
        xs.set(p.x, "edge");
      }
      verticalLines.set(y, xs);
    }
  }
}

function enclosed(p: Point): boolean {
  if (horizontalLines.get(p.x)?.has(p.y) || verticalLines.get(p.y)?.has(p.x)) {
    return true;
  }
  
  // Count crossings with vertical segments to the right.
  // Two corners count as one wall (continuous horizontal line), so we check if (corners/2 + edges) is odd
  let cornersRight = 0;
  let edgesRight = 0;
  verticalLines.get(p.y)?.forEach((value, x) => {
    if (x > p.x) {
      if (value === "corner") cornersRight++;
      else edgesRight++;
    }
  });

  // In theory we could check above, left, and below, but it seems to not be necessary.
  
  return (cornersRight / 2 + edgesRight) % 2 === 1;
}

function* border(p1: Point, p2: Point): Generator<Point> {
  const minX = Math.min(p1.x, p2.x);
  const maxX = Math.max(p1.x, p2.x);
  const minY = Math.min(p1.y, p2.y);
  const maxY = Math.max(p1.y, p2.y);
  
  const corners = [
    { x: minX, y: minY }, // bottom-left
    { x: maxX, y: minY }, // bottom-right
    { x: maxX, y: maxY }, // top-right
    { x: minX, y: maxY }, // top-left
  ];
  
  for (const corner of corners) {
    yield corner;
  }
  yield* pointsBetween(corners[0], corners[1]);
  yield* pointsBetween(corners[1], corners[2]);
  yield* pointsBetween(corners[2], corners[3]);
  yield* pointsBetween(corners[3], corners[0]);
}

function rectangleEnclosed(p1: Point, p2: Point): boolean {
  for (const b of border(p1, p2)) {
    if (!enclosed(b)) {
      return false;
    }
  }
  return true;
}

let largestArea = 0;
let lastPercentageLogged = 0;
for (let i = 0; i < areas.length; i++) {
  const area = areas[i];
  const percentage = (i / areas.length) * 100;
  if (percentage - lastPercentageLogged >= 1) {
    // console.log("Checking area " + i + " of " + areas.length + " (" + percentage + "%)");
    lastPercentageLogged = percentage;
  }
  if (rectangleEnclosed(area.p1, area.p2)) {
    largestArea = area.area;
    break;
  }
}
console.log("Part 2: " + largestArea);
