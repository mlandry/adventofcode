import { readLines } from "../utils/utils";

type Point = {
  x: number;
  y: number;
  z: number;
}

function parsePoint(line: string): Point {
  const [x, y, z] = line.split(',').map(Number);
  return { x, y, z };
}

function distance(p1: Point, p2: Point): number {
  return Math.sqrt((p1.x - p2.x) ** 2 + (p1.y - p2.y) ** 2 + (p1.z - p2.z) ** 2);
}

const NUM_CONNECTIONS = 1000;
const points = readLines(import.meta.url, "input.txt").map(parsePoint);

const distances: { d: number, p1: Point, p2: Point }[] = [];
for (let i = 0; i < points.length; i++) {
  const p1 = points[i];
  for (let j = i + 1; j < points.length; j++) {
    const p2 = points[j];
    const d = distance(p1, p2);
    distances.push({ d, p1, p2 });
  }
}
distances.sort((a, b) => a.d - b.d);

type Circuit = Set<Point>;
const circuitMap = new Map<Point, Circuit>();
for (const p of points) {
  circuitMap.set(p, new Set([p]));
}

for (let i = 0; i < NUM_CONNECTIONS; i++) {
  const { p1, p2 } = distances[i];
  const c1 = circuitMap.get(p1)!;
  const c2 = circuitMap.get(p2)!;
  if (c1 === c2) {
    continue;
  }
  const merged = new Set([...c1, ...c2]);
  for (const p of merged) {
    circuitMap.set(p, merged);
  }
}

const circuitSizes = Array.from(new Set(circuitMap.values())).map(c => c.size);
circuitSizes.sort((a, b) => b - a);
const mult = circuitSizes[0] * circuitSizes[1] * circuitSizes[2];
console.log("Part 1: " + mult);

for (let i = NUM_CONNECTIONS; i < distances.length; i++) {
  const { p1, p2 } = distances[i];
  const c1 = circuitMap.get(p1)!;
  const c2 = circuitMap.get(p2)!;
  if (c1 === c2) {
    continue;
  }
  const merged = new Set([...c1, ...c2]);
  if (merged.size === points.length) {
    const mult = p1.x * p2.x;
    console.log("Part 2: " + mult);
    break;
  }
  for (const p of merged) {
    circuitMap.set(p, merged);
  }
}