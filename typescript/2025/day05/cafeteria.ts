import { readLines } from "../utils/utils";

export class Range {
  constructor(public start: number, public end: number) {}

  contains = (x: number) => {
    return x >= this.start && x <= this.end;
  }

  overlaps = (other: Range) => {
    return this.start <= other.end && this.end >= other.start;
  }

  adjacent = (other: Range) => {
    return this.end + 1 === other.start || other.end + 1 === this.start;
  }

  size = () => {
    return this.end - this.start + 1;
  }
}

const lines = readLines(import.meta.url, "input.txt");

let freshDone = false;
const fresh: Range[] = [];
const ingredients: number[] = [];
for (const line of lines) {
  if (line.length === 0) {
    freshDone = true;
    continue;
  }

  if (!freshDone) {
    const [start, end] = line.split("-");
    fresh.push(new Range(Number.parseInt(start), Number.parseInt(end)));
  } else {
    ingredients.push(Number.parseInt(line));
  }
}

const available = ingredients.filter(i => fresh.some(r => r.contains(i)));
console.log("Part 1: " + available.length);

function mergeWith(candidate: Range, ranges: Range[]): { merged: Range, remaining: Range[] } {
  for (let i = 0; i < ranges.length; i++) {
    const range = ranges[i];
    if (candidate.overlaps(range) || candidate.adjacent(range)) {
      const merged = new Range(Math.min(candidate.start, range.start), Math.max(candidate.end, range.end));
      const remaining = [...ranges.slice(0, i), ...ranges.slice(i + 1)];
      return mergeWith(merged, remaining);
    }
  }
  return { merged: candidate, remaining: ranges };
}

let merged: Range[] = [];
let remaining = [...fresh];
while (remaining.length > 0) {
  const candidate = remaining[0];
  const { merged: mergedRange, remaining: remainingRanges } = mergeWith(candidate, remaining.slice(1));
  merged.push(mergedRange);
  remaining = remainingRanges;
}

const totalSize = merged.reduce((acc, range) => acc + range.size(), 0);
console.log("Part 2: " + totalSize);

